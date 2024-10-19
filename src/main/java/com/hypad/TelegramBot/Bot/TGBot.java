package com.hypad.TelegramBot.Bot;

import com.hypad.TelegramBot.model.Category;
import com.hypad.TelegramBot.repository.CategoryRepo;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;
import java.util.Optional;

@Component
@PropertySources({@PropertySource("classpath:application.properties")})
public class TGBot extends TelegramLongPollingBot {

    private final CategoryRepo categoryRepo;
    @Value("${telegram.bot.username}")
    private String botName;

    @Value("${telegram.bot.token}")
    private String botToken;

    public TGBot(CategoryRepo categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()){
            System.out.println("Received update: " + update);

            String msg = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String[] commandParts = msg.split(" ");

            switch (commandParts[0]) {
                case "/viewTree" -> viewTree(chatId);
                case "/addElement" -> addElem(chatId, commandParts);
                case "/removeElement" -> removeElem(chatId, commandParts);
                case "/help" -> help(chatId);
                default -> sendMessage(chatId, "Unknown command! Use /help to see the list of available commands.");
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Transactional
    private void viewTree(Long chatId){
        List<Category> rootCategories = categoryRepo.findByParentIsNull();
        if (rootCategories.isEmpty()){
            sendMessage(chatId, "The category tree is empty");
            return;
        }

        StringBuilder treeBuilder = new StringBuilder("Category Tree:\n");
        for (Category root : rootCategories) {
            appendCategoryTree(treeBuilder, root, 0);
        }
        sendMessage(chatId, treeBuilder.toString());
    }
    private void appendCategoryTree(StringBuilder treeBuilder, Category category, int level) {
        String indent = "  ".repeat(level);
        treeBuilder.append(indent).append("- ").append(category.getName()).append("\n");

        for (Category child : category.getChildren()) {
            appendCategoryTree(treeBuilder, child, level + 1);
        }
    }
    private void addElem(Long chatId, String[] commandParts){
        if(commandParts.length == 2){
            String categoryName = commandParts[1];
            Optional<Category> existingCat = categoryRepo.findByName(categoryName);
            if(existingCat.isPresent()){
                sendMessage(chatId, "Category already exists");
                return;
            }
            Category category = Category
                    .builder()
                    .name(categoryName).build();
            categoryRepo.save(category);
            sendMessage(chatId, "Root category added: " + categoryName);
        } else if (commandParts.length == 3) {
            String parentN = commandParts[1];
            String childN = commandParts[2];

            Optional<Category> parent = categoryRepo.findByName(parentN);
            if(parent.isEmpty()){
                sendMessage(chatId, "Parent category doesnt exists");
                return;
            }

            Optional<Category> child = categoryRepo.findByName(childN);
            if(child.isPresent()){
                sendMessage(chatId, "Child category already exists");
                return;
            }

            Category childCat = Category
                    .builder()
                    .name(childN)
                    .parent(parent.get()).build();

            categoryRepo.save(childCat);

            sendMessage(chatId, "Child category added under: " + parentN);
        } else{
            sendMessage(chatId, "Invalid command format. Use /addElement <parent> <child>.");
        }
    }

    private void removeElem(Long chatId, String[] commandParts){
        if (commandParts.length != 2){
            sendMessage(chatId, "Invalid command format. Use /removeElement <element>.");
            return;
        }

        String catName = commandParts[1];
        Optional<Category> categoryOptional = categoryRepo.findByName(catName);

        if (categoryOptional.isEmpty()) {
            sendMessage(chatId, "Category not found.");
            return;
        }

        Category categoryToRemove = categoryOptional.get();
        categoryRepo.delete(categoryToRemove);
        sendMessage(chatId, "Category '" + catName + "' and its subtree have been removed.");
    }

    private void help(Long chatId){
        String helpMessage = """
                /viewTree - View the category tree
                /addElement <element> - Add a root element
                /addElement <parent> <child> - Add a child element to an existing parent
                /removeElement <element> - Remove an element and its subtree
                /help - Show this help message
                """;
        sendMessage(chatId, helpMessage);
    }

    private void sendMessage(Long chatId, String text){
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try{
            execute(message);
        }catch (TelegramApiException e){
            e.printStackTrace();
        }
    }
    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException{
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        try {
            api.registerBot(this);
        }
        catch (TelegramApiException e){
            e.printStackTrace();
        }
    }
}
