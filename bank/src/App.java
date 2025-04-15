import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
class accounts{
    private Connection connection;
    private Scanner sc;
    public accounts(Connection connection,Scanner sc){
        this.connection=connection;
        this.sc=sc;
    }
    public boolean account_exists(String username){
        String query="select account_number from accounts where username = ?";
        try {
            PreparedStatement ps=connection.prepareStatement(query);
            ps.setString(1, username);
            ResultSet rs =ps.executeQuery();
            if(rs.next()){
                return true;
                //true hai matlab account exists karta hai
                //rs.next matlab jab tak data hai
            }
            else{
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    private long generateaccountnumber(){
        try {
            Statement st=connection.createStatement();
            ResultSet rs=st.executeQuery("select account_number from accounts order by account_number desc limit 1");
            if(rs.next()){
                long last_account_number = rs.getLong("account_number");
                return last_account_number+1;
            }
            else{
                return 10010001;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 10010001;
    }
    public long getAccount_number(String username) {
        String query = "SELECT account_number from accounts WHERE username = ?";
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                return resultSet.getLong("account_number");
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        throw new RuntimeException("Account Number Doesn't Exist!");
    }


    public long open_accounts(String username){
        if(!account_exists(username)){
            String aq ="insert into accounts(account_number,username,name,balance,pin) values(?,?,?,?,?)";
            sc.nextLine();
            System.out.print("ENTER YOUR FULL NAME:-");
            String name =sc.nextLine();
            System.out.print("ENTER THE INITIAL AMOUNT:-");
            double balance = sc.nextDouble();
            sc.nextLine();
            System.out.print("Enter The Pin: ");
            String pin = sc.nextLine();
            try {
                long account_number = generateaccountnumber();
                PreparedStatement ps = connection.prepareStatement(aq);
                ps.setLong(1, account_number);
                ps.setString(2, username);
                ps.setString(3, name);
                ps.setDouble(4, balance);
                ps.setString(5, pin);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    return account_number;
                } else {
                    throw new RuntimeException("Account Creation failed!!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        throw new RuntimeException("Account Already Exists!");
    }
}
class accountmanager{
    private Connection connection;
    private Scanner sc;
    public accountmanager(Connection connection,Scanner sc){
        this.connection=connection;
        this.sc=sc;
    }
    public void debit(long account_number) throws SQLException{
        sc.nextLine();
        System.out.print("Enter the amount:-");
        double amount = sc.nextDouble();
        sc.nextLine();
        System.out.print("ENTER THE PIN:-");
        String pin =sc.nextLine();
        try {
            connection.setAutoCommit(false);
            if(account_number!=0){
                PreparedStatement ps =connection.prepareStatement("select * from accounts where account_number = ? and pin = ?");
                ps.setLong(1, account_number);
                ps.setString(2, pin);
                ResultSet rs=ps.executeQuery();
                if(rs.next()){
                    double cb =rs.getDouble("balance");
                    if(amount<=cb){
                        String dq ="update accounts set balance =balance - ? where account_number = ?";
                        PreparedStatement ps1 = connection.prepareStatement(dq);
                        ps1.setDouble(1, amount);
                        ps1.setLong(2, account_number);
                        int rowsAffected =ps1.executeUpdate();
                        if(rowsAffected>0){
                            System.out.println("RS."+amount+"DEBITED SUCCESFULLY");
                            connection.commit();
                            connection.setAutoCommit(true);
                        }
                        else{
                            System.out.println("TRANSACTION FAILED!");
                            connection.rollback();
                            connection.setAutoCommit(true);
                        }
                    
                    }
                    else{
                        System.out.println("INSUFFICIENT BALANCE");
                    }

                }
                else{
                    System.out.println("INVALID PIN");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        connection.setAutoCommit(true);
    }
    public void credit(long account_number) throws SQLException{
        sc.nextLine();
        System.out.print("Enter the amount:-");
        double amount = sc.nextDouble();
        sc.nextLine();
        System.out.print("ENTER THE PIN:-");
        String pin =sc.nextLine();
        try {
            connection.setAutoCommit(false);
            if(account_number!=0){
                PreparedStatement ps =connection.prepareStatement("select * from accounts where account_number = ? and pin = ?");
                ps.setLong(1, account_number);
                ps.setString(2, pin);
                ResultSet rs=ps.executeQuery();
                if(rs.next()){
                    double cb =rs.getDouble("balance");
                    String cq ="update accounts set balance =balance + ? where account_number = ?";
                    PreparedStatement ps1 = connection.prepareStatement(cq);
                    ps1.setDouble(1, amount);
                    ps1.setLong(2, account_number);
                    int rowsAffected =ps1.executeUpdate();
                    if(rowsAffected>0){
                        System.out.println("RS."+amount+"CREDITED SUCCESFULLY");
                        connection.commit();
                        connection.setAutoCommit(true);
                    }
                    else{
                        System.out.println("TRANSACTION FAILED!");
                        connection.rollback();
                        connection.setAutoCommit(true);
                    }

                }
                else{
                    System.out.println("INVALID PIN");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        connection.setAutoCommit(true);
    }
    public void getBalance(long account_number){
        sc.nextLine();
        System.out.print("Enter Security Pin: ");
        String pin = sc.nextLine();
        try{
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT balance FROM accounts WHERE account_number = ? AND pin = ?");
            preparedStatement.setLong(1, account_number);
            preparedStatement.setString(2, pin);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                double balance = resultSet.getDouble("balance");
                System.out.println("Balance: "+balance);
            }else{
                System.out.println("Invalid Pin!");
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    public void transfermoney(long sender_account_number) throws SQLException{
        sc.nextLine();
        System.out.print("Enter Receiver Account Number: ");
        long receiver_account_number = sc.nextLong();
        sc.nextLine();
        System.out.print("Enter Amount: ");
        double amount = sc.nextDouble();
        sc.nextLine();
        System.out.print("Enter Pin: ");
        String security_pin = sc.nextLine();

        try {
            connection.setAutoCommit(false);
            if(sender_account_number !=0 && receiver_account_number !=0){
                PreparedStatement ps= connection.prepareStatement("select * from accounts where account_number = ? and pin = ?");
                ps.setLong(1, sender_account_number);
                ps.setString(2, security_pin);
                ResultSet rs =ps.executeQuery();
                if (rs.next()) {
                    double current_balance = rs.getDouble("balance");
                    if (amount<=current_balance){

                        // Write debit and credit queries
                        String debit_query = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
                        String credit_query = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";

                        // Debit and Credit prepared Statements
                        PreparedStatement creditPreparedStatement = connection.prepareStatement(credit_query);
                        PreparedStatement debitPreparedStatement = connection.prepareStatement(debit_query);

                        // Set Values for debit and credit prepared statements
                        creditPreparedStatement.setDouble(1, amount);
                        creditPreparedStatement.setLong(2, receiver_account_number);
                        debitPreparedStatement.setDouble(1, amount);
                        debitPreparedStatement.setLong(2, sender_account_number);
                        int rowsAffected1 = debitPreparedStatement.executeUpdate();
                        int rowsAffected2 = creditPreparedStatement.executeUpdate();
                        if (rowsAffected1 > 0 && rowsAffected2 > 0) {
                            System.out.println("Transaction Successful!");
                            System.out.println("Rs."+amount+" Transferred Successfully");
                            connection.commit();
                            connection.setAutoCommit(true);
                            return;
                        } else {
                            System.out.println("Transaction Failed");
                            connection.rollback();
                            connection.setAutoCommit(true);
                        }
                    }else{
                        System.out.println("Insufficient Balance!");
                    }
                }else{
                    System.out.println("Invalid Security Pin!");
                }
            }else{
                System.out.println("Invalid account number");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        connection.setAutoCommit(true);
    }
}
class user{
    private Connection connection;
    private Scanner sc;
    public user(Connection connection,Scanner sc){
        this.connection=connection;
        this.sc=sc;
    }
    public void register(){
        sc.nextLine();
        System.out.print("Username:-");
        String username=sc.nextLine();
        System.out.print("Password:-");
        String password=sc.nextLine();
        if (user_exists(username)){
            System.out.println("Username already exists");
            return;
        }
        String rq="insert into user(username, password) values (?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(rq);
            ps.setString(1, username);
            ps.setString(2, password);
            int ar=ps.executeUpdate();
            if(ar>0){
                System.out.println("Registration Succesfull");
            }
            else{
                System.out.println("Registration Unsuccesfull");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public String login(){
        sc.nextLine();
        System.out.print("Username:-");
        String username=sc.nextLine();
        System.out.print("Password:-");
        String password=sc.nextLine();
        String lq="select * from user where username = ? and password = ?";
        try {
            PreparedStatement ps =connection.prepareStatement(lq);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return username;
            }
            else{
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean user_exists(String username){
        String que ="select * from user where username = ?";
        try {
            PreparedStatement ps =connection.prepareStatement(que);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return true;
            }
            else{
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
public class App {
    public static void main(String[] args) throws Exception {
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        }catch (ClassNotFoundException e){
            System.out.println(e.getMessage());
        }
        try {
            String url ="jdbc:mysql://localhost:3306/bank";

            String userName ="root";
            String password ="Aditya@123";
            Connection conn =DriverManager.getConnection(url,userName,password);
            Statement stm = conn.createStatement();
            Scanner sc =new Scanner(System.in);
            user user =new user(conn,sc);
            accounts accounts =new accounts(conn, sc);
            accountmanager accountmanager=new accountmanager(conn, sc);
            String username;
            long account_number;
            while(true){
                System.out.println("*** WELCOME TO BANKING SYSTEM ***");
                System.out.println();
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.println("Enter your choice: ");
                int choice1 = sc.nextInt();
                switch (choice1){
                    case 1:
                        user.register();
                        break;
                    case 2:
                        username = user.login();
                        if(username!=null){
                            System.out.println();
                            System.out.println("User Logged In!");
                            if(!accounts.account_exists(username)){
                                System.out.println();
                                System.out.println("1. Open a new Bank Account");
                                System.out.println("2. Exit");
                                if(sc.nextInt() == 1) {
                                    account_number = accounts.open_accounts(username);
                                    System.out.println("Account Created Successfully");
                                    System.out.println("Your Account Number is: " + account_number);
                                }else{
                                    break;
                                }

                            }
                            account_number = accounts.getAccount_number(username);
                            int choice2 = 0;
                            while (choice2 != 5) {
                                System.out.println();
                                System.out.println("1. Debit Money");
                                System.out.println("2. Credit Money");
                                System.out.println("3. Transfer Money");
                                System.out.println("4. Check Balance");
                                System.out.println("5. Log Out");
                                System.out.println("Enter your choice: ");
                                choice2 = sc.nextInt();
                                switch (choice2) {
                                    case 1:
                                        accountmanager.debit(account_number);
                                        break;
                                    case 2:
                                        accountmanager.credit(account_number);
                                        break;
                                    case 3:
                                        accountmanager.transfermoney(account_number);
                                        break;
                                    case 4:
                                        accountmanager.getBalance(account_number);
                                        break;
                                    case 5:
                                        break;
                                    default:
                                        System.out.println("Enter Valid Choice!");
                                        break;
                                }
                            }

                        }
                        else{
                            System.out.println("Incorrect Email or Password!");
                        }
                    case 3:
                        System.out.println("THANK YOU FOR USING BANKING SYSTEM!!!");
                        System.out.println("Exiting System!");
                        return;
                    default:
                        System.out.println("Enter Valid Choice");
                        break;
                }
            }
            
            // System.out.println("Connection Successfull");
            //String query ="";
            // stm.execute(query);
            // System.out.println("Database Created Succesfully!");
            // conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
