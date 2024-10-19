The bot supports basic commands such as /viewTree, /addElement, and /removeElement.

Key Features:
View category tree: Displays a hierarchical list of categories.
Add category elements: Allows the addition of root or child categories.
Remove category elements: Deletes categories along with their subcategories.
Help command: Provides a list of available commands.

Project Structure
The project consists of three main components:

Bot Class (TGBot.java): Handles Telegram bot interactions, receives updates, and processes commands.
Repository Interface (CategoryRepo.java): Provides methods for accessing the database.
Model Class (Category.java): Represents the category entity with a hierarchical structure.
Dependencies:
Spring Framework: For dependency injection and event handling.
Spring Data JPA: To interact with the database.
Hibernate: ORM tool for database management.
Telegram Bots API: Java library for Telegram bot integration.

Commands Overview
1. /viewTree
Description: Displays the entire category tree in a hierarchical format.
Usage: /viewTree
2. /addElement
Description: Adds a root category or a child category.
Usage:
To add a root category: /addElement <category>
To add a child category: /addElement <parent> <child>
3. /removeElement
Description: Removes a category and its subtree.
Usage: /removeElement <category>
4. /help
Description: Displays a list of available commands.
Usage: /help
