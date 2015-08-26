package de.mauricius17.hideplayers.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

import de.mauricius17.hideplayers.listener.HidePlayersListener.Groups;

public class MySQL_HidePlayers {

	public static void showGroup(String uuid, Groups group) {
		MySQL.getExecutor().execute(new Runnable() {
			
			@Override
			public void run() {
				getHiddenGroups(uuid, new Consumer<String>() {

					@Override
					public void accept(String result) {
						try {
							if(!result.equals("wrong")) {
								String[] res = result.split(";");
								
								String g = "";
								
								for(int i = 0; i < res.length; i++) {
									if(!res[i].equals(group.toString())) {
										g = g + res[i] + ";";
									}
								}		
								
								String qry = "UPDATE " + MySQL.getTABLE() + " SET " + MySQL.getHIDDEN() + " = ? WHERE playeruuid = ?";
								PreparedStatement stmt = MySQL.getConnection().prepareStatement(qry);
								stmt.setString(1, g);
								stmt.setString(2, uuid);
								stmt.executeUpdate();
								stmt.close();
							}	
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				});
			}
		});
	}
	
	public static void hideGroup(String uuid, Groups group) {
		MySQL.getExecutor().execute(new Runnable() {
			
			@Override
			public void run() {
				getHiddenGroups(uuid, new Consumer<String>() {

					@Override
					public void accept(String result) {
						try {
							if(result.equals("wrong")) {
								String qry = "INSERT INTO " + MySQL.getTABLE() + " (playeruuid, " + MySQL.getHIDDEN() + ") VALUES (?,?)";
								PreparedStatement stmt = MySQL.getConnection().prepareStatement(qry);
								stmt.setString(1, uuid);
								stmt.setString(2, group.toString() + ";");
								stmt.executeUpdate();
								stmt.close();
							} else {
								String qry = "UPDATE " + MySQL.getTABLE() + " SET " + MySQL.getHIDDEN() + " = ? WHERE playeruuid = ?";
								PreparedStatement stmt = MySQL.getConnection().prepareStatement(qry);
								stmt.setString(1, result + group.toString() + ";");
								stmt.setString(2, uuid);
								stmt.executeUpdate();
								stmt.close();
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				});
			}
		});
	}
	
	public static void getHiddenGroups(String uuid, Consumer<String> consumer) {
		MySQL.getExecutor().execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					String qry = "SELECT " + MySQL.getHIDDEN() + " FROM " + MySQL.getTABLE() + " WHERE playeruuid = ?"; 
					PreparedStatement stmt = MySQL.getConnection().prepareStatement(qry);
					stmt.setString(1, uuid);
					
					ResultSet rs = stmt.executeQuery();
					
					if(rs.next()) {
						consumer.accept(rs.getString(MySQL.getHIDDEN()));
					} else {
						consumer.accept("wrong");
					}
					
					rs.close();
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}
}