package com.ykb.sccm.CMUpdate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;

public class EnvanterApp {
	private String userName = "xxxx";
	
	private String password = "xxxx";
	String encodedString = Base64.getEncoder().encodeToString(password.getBytes());
	byte[] dbBad = Base64.getDecoder().decode("xxxx");
	String dbBadPw = new String(dbBad);	
	
	private String jdbcCAURL = "jdbc:jtds:sqlserver://server_name:port/db_name;instance=instance_name";
	String dbUserName = "xxxx";
	byte[] dbDe = Base64.getDecoder().decode("xxxx");
	String dbwd = new String(dbDe);

	public static void main(String[] args) {
		new EnvanterApp().dbConn();
	}

	public void dbConn() {
		String jdbcURL = "jdbc:jtds:sqlserver://server_name:port/db_name";
		try (Connection connection = DriverManager.getConnection(jdbcURL, userName, dbBadPw)) {

			String reqSql = "Select * from (select gs.Name0 CompName,gs.Manufacturer0 Firma,Replace(gs.Model0,'''','') Model,gs.UserName0 SonLogin,process.Name0 CPU,CAST(pm.TotalPhysicalMemory0/1024/1024.0 AS DECIMAL(15)) AS TotalMemoryGB,BS.SerialNumber0 SeriNo,HW.LastHWScan SonGuncellemeTarihi"
					+ " from CM_CMY.dbo.v_GS_COMPUTER_SYSTEM gs"
					+ " left outer join CM_CMY.dbo.v_GS_PROCESSOR process on gs.ResourceID = process.ResourceID"
					+ " left outer join CM_CMY.dbo.v_GS_X86_PC_MEMORY pm on gs.ResourceID = pm.ResourceID"
					+ " left outer join CM_CMY.dbo.v_GS_PC_BIOS BS on gs.ResourceID = BS.ResourceID"
					+ " left outer join CM_CMY.dbo.v_GS_WORKSTATION_STATUS HW on gs.ResourceID =HW.ResourceID "
					+ "where DomainRole0=1 " + ") Son where " + "SeriNo is not null and SeriNo<>''";
			Statement statement1 = connection.createStatement();
			ResultSet reqResult = statement1.executeQuery(reqSql);

			String countSql = "Select count(*) count from (select gs.Name0 CompName,gs.Manufacturer0 Firma,Replace(gs.Model0,'''','') Model,gs.UserName0 SonLogin,process.Name0 CPU,CAST(pm.TotalPhysicalMemory0/1024/1024.0 AS DECIMAL(15)) AS TotalMemoryGB,BS.SerialNumber0 SeriNo,HW.LastHWScan SonGuncellemeTarihi"
					+ " from CM_CMY.dbo.v_GS_COMPUTER_SYSTEM gs"
					+ " left outer join CM_CMY.dbo.v_GS_PROCESSOR process on gs.ResourceID = process.ResourceID"
					+ " left outer join CM_CMY.dbo.v_GS_X86_PC_MEMORY pm on gs.ResourceID = pm.ResourceID"
					+ " left outer join CM_CMY.dbo.v_GS_PC_BIOS BS on gs.ResourceID = BS.ResourceID"
					+ " left outer join CM_CMY.dbo.v_GS_WORKSTATION_STATUS HW on gs.ResourceID =HW.ResourceID "
					+ "where DomainRole0=1 " + ") Son where " + "SeriNo is not null and SeriNo<>''";
			Statement statement = connection.createStatement();
			ResultSet countResult = statement.executeQuery(countSql);
			countResult.next();
			int count = countResult.getInt("count");
			statement.close();
			
			if (count > 5000) {
				Connection connectDelete=DriverManager.getConnection(jdbcCAURL, dbUserName, dbwd);
				String deleteSql = "Delete from MDB.Servicedesk.zSCCM2CM";
				PreparedStatement deleteStmt = connectDelete.prepareStatement(deleteSql);
				deleteStmt.execute();
				deleteStmt.close();
				Connection connectionInsert = DriverManager.getConnection(jdbcCAURL, dbUserName, dbwd);
				
				while (reqResult.next()) {
					String compName = reqResult.getString("CompName");
					String firma = reqResult.getString("Firma");
					String model = reqResult.getString("Model");
					String sonLogin = reqResult.getString("SonLogin");
					String CPU = reqResult.getString("CPU");
					String TotalMemoryGB = reqResult.getString("TotalMemoryGB");
					// Integer TotalMemory=Integer.parseInt(TotalMemoryGB);
					String SeriNo = reqResult.getString("SeriNo");
					String SonGuncellemeTarihi = reqResult.getString("SonGuncellemeTarihi");
					dbUpdate(compName, firma, model, sonLogin, CPU, TotalMemoryGB, SeriNo, SonGuncellemeTarihi,connectionInsert);
				}
				statement1.close();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void dbUpdate(String compName, String firma, String model, String sonLogin, String CPU, String TotalMemoryGB,
			String SeriNo, String SonGuncellemeTarihi,Connection connectionInsert) throws SQLException {
		
			String query = "Insert into MDB.Servicedesk.zSCCM2CM(CompName,Firma,Model,SonLogin,CPU,Memory,SeriNo,SonGuncelleme,islemtarihi) values "
					+ "('" + compName + "','" + firma + "','" + model + "','" + sonLogin + "','" + CPU + "','"
					+ TotalMemoryGB + "','" + SeriNo + "','" + SonGuncellemeTarihi + "',GetDate())";

			PreparedStatement preparedStmt = connectionInsert.prepareStatement(query);
			preparedStmt.execute();
			preparedStmt.close();


	}
}
