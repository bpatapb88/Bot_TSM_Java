package com.simanov;
import org.telegram.telegrambots.meta.api.objects.User;

import java.sql.*;

public class DatabaseHandler extends Configs{
    Connection dbConnection;

    public Connection getDbConnection() throws ClassNotFoundException, SQLException{
        String connectionString = "jdbc:postgresql://" + dbHost + ":"
                + dbPort + "/" + dbName;
        Class.forName("org.postgresql.Driver");
        if(dbConnection == null){
            dbConnection = DriverManager.getConnection(connectionString, dbUser, dbPass);
        }
        return dbConnection;
    }

    public void incrementMessagesDB(Long userId){
        Long currentChatValue = getSocialValue(userId, "Chat");
        System.out.println("Current messages " + currentChatValue);
        currentChatValue++;
        String sqlCommandUpdate = "UPDATE users_tsm SET values = jsonb_set(values::jsonb,'{\"Social\",\"Chat\"}'," +
                currentChatValue + "::text::jsonb, false) WHERE telegram_id=" +
                userId + ";";
        executeQuery(sqlCommandUpdate);
        System.out.println("incrementMessagesDB done");
    }

    public Long getSocialValue(Long userId, String social){
        System.out.println("getSocialValue " + social);
        Long currentValue = 0L;
        String select = "SELECT values -> 'Social' -> '" + social + "' FROM users_tsm WHERE telegram_id=" + userId + ";";
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(select);
            ResultSet resultSet = prSt.executeQuery();
            if(resultSet.next()){
                currentValue = resultSet.getLong(1);
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return currentValue;
    }

    private boolean ifUserExist(User user){
        Long userId = user.getId();
        System.out.println("ifUserExist " + user);
        String select = "SELECT telegram_id,user_name FROM users_tsm WHERE telegram_id=" + userId + ";";
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(select);
            ResultSet resultSet = prSt.executeQuery();
            return resultSet.next();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public void registerUserInDB(User user){
        if(ifUserExist(user)){
            System.out.println("User " + user.getFirstName() + " exists");
            return;
        }
        String userName = user.getUserName() != null ? user.getUserName() : user.getFirstName();
        String sqlInsert = "INSERT INTO users_tsm (telegram_id,user_name, karma) VALUES (" + user.getId() + ",'" + userName + "', 0)";
        String sqlUpdate = "UPDATE users_tsm SET values = json_build_object ('Events',json_build_object('BoardGame',0,'HikingTrip',0,'Creative',0,'Others',0)," +
                "'Social', json_build_object('Attended',0,'Chat',0,'InvitedFriends',0)," +
                "'Media', json_build_object('Meme',0,'Content',0)," +
                "'Hidden', json_build_object('exit',0,'horny',0)," +
                "'Achievements',json_build_array()) " +
                "WHERE telegram_id=" + user.getId() + ";";
        executeQuery(sqlInsert);
        executeQuery(sqlUpdate);
        System.out.println("UserId " + user.getId() + " was registered.");
    }

    public void incrementInvited(Long invitedById) {
        System.out.println("incrementInvited id " + invitedById);
        Long currentInvitedValue = getSocialValue(invitedById, "InvitedFriends");
        currentInvitedValue++;
        String sqlCommand = "UPDATE users_tsm SET values = jsonb_set(values::jsonb,'{\"Social\",\"InvitedFriends\"}'," + currentInvitedValue +
                "::text::jsonb, false) WHERE telegram_id=" + invitedById + ";";
        executeQuery(sqlCommand);
        System.out.println("incrementInvited[EXIT] id " + invitedById);
    }

    private void executeQuery(String query){
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(query);
            prSt.executeUpdate();
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    public int changeKarmaDB(User nominated, boolean raise) {
        System.out.println("changeKarma [ENTER] telegram_id=" + nominated.getId() + "\nraise=" + raise);
        int karma = getKarma(nominated);
        if(raise){
            karma++;
        }else{
            if (karma > 0){
                karma--;
            }
        }
        String sqlUpdate = "UPDATE users_tsm SET karma=" + karma + " WHERE telegram_id=" + nominated.getId() + ";";
        executeQuery(sqlUpdate);
        System.out.println("changeKarma [EXIT] telegram_id=" + nominated.getId() +
                "\nraise=" + raise +
                "\nnewKarma=" + karma);
        return karma;
    }

    public int getKarma(User nominated) {
        System.out.println("getKarma for user " + nominated);
        int currentKarma = 0;
        String select = "SELECT karma FROM users_tsm WHERE telegram_id=" + nominated.getId() + ";";
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(select);
            ResultSet resultSet = prSt.executeQuery();
            if(resultSet.next()){
                currentKarma = resultSet.getInt(1);
            }else{
                System.out.println("Something wrong");
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return currentKarma;
    }
}
