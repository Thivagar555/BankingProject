# Bank System Simulator

This is a comprehensive, console-based banking application built in Java. It simulates the core functionalities of a banking system, including secure user/admin roles, financial transactions, and database logging.

This project connects to a *MySQL* database using *JDBC* and is built on a professional *3-Tier (Layered) Architecture* to ensure clean, manageable, and scalable code.

## Key Features

* *3-Tier Architecture:* Code is strictly separated into Presentation (UI), Business Logic (Services), and Data Access (DAO) layers.
* *Dual User Roles:*
    * *Customer Role:* Can create an account, log in, deposit, withdraw, transfer, and view their transaction history.
    * *Admin Role:* Can log in, view all user accounts, search for users, view system-wide statistics, and manage user accounts (freeze/unfreeze, delete, update).
* *Security:*
    * *Password Hashing:* All user and admin passwords are hashed using *SHA-256*.
    * *Input Validation:* User inputs for emails, phone numbers, and password strength are validated.
    * *Account Freezing:* Admin can freeze and unfreeze user accounts.
* *Database & Transactions:*
    * *MySQL & JDBC:* All account and transaction data is stored in a MySQL database.
    * *Transaction Logging:* Every deposit, withdrawal, and transfer is recorded in a separate transactions table to generate a mini-statement.

## Technology Stack

* *Language:* Java (JDK 17+)
* *Database:* MySQL
* *Connectivity:* JDBC (via MySQL Connector/J)
* *IDE:* IntelliJ IDEA

## How to Run This Project

### 1. Database Setup
1.  Open MySQL Workbench (or any MySQL client).
2.  Create a new database schema:
    sql
    CREATE DATABASE banking_simulator;
    
3.  You do *not* need to create the tables. The Java application will do this automatically on its first run (in the initializeDatabase method).

### 2. Configure the Project
1.  Open the project in IntelliJ IDEA.
2.  Navigate to org.example.BankSystemIntegrated.java.
3.  Find this line (around line 19):
    java
    private static final String DB_PASS = "openssl rand -base64 18";
    
4.  *This is the most important step:* Change "openssl rand -base64 18" to your *actual MySQL password*.
    * *Example:* private static final String DB_PASS = "MyRootPassword123!";

### 3. Add the JDBC Driver
1.  *Download* the MySQL Connector/J .jar file from the official [MySQL website](https://dev.mysql.com/downloads/connector/j/). (Select "Platform Independent" and get the ZIP archive).
2.  Unzip the file and find the .jar file.
3.  In IntelliJ, go to *File* $\rightarrow$ *Project Structure...*
4.  Select *Libraries* from the side menu.
5.  Click the **+ (plus) icon** $\rightarrow$ *Java*.
6.  Find and select the mysql-connector-j-x.x.x.jar file you downloaded.
7.  Click *OK* on both windows.

### 4. Run the Application
1.  Open the BankSystemIntegrated.java file.
2.  Right-click in the file and select *"Run 'BankSystemIntegrated.main()'"*.
3.  The application will start in your console, connect to the database, create the tables, and show the main menu.

## Default Admin Login
The system automatically creates a default admin user for you.
* *Username:* admin
* *Password:* admin
