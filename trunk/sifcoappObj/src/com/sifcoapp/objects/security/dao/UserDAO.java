package com.sifcoapp.objects.security.dao;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

import com.sifcoapp.objects.catalogos.Common;
import com.sifcoapp.objects.common.dao.commonDAO;
import com.sifcoapp.objects.common.to.DBManager;
import com.sifcoapp.objects.security.to.ProfileInTO;
import com.sifcoapp.objects.security.to.ProfileOutTO;
import com.sifcoapp.objects.security.to.UserAppInTO;
import com.sifcoapp.objects.security.to.UserAppOutTO;

public class UserDAO extends commonDAO {
	private DBManager dbManager;
	
	public UserDAO(){
		super();
	}
	
	public UserAppOutTO getUserValid(UserAppInTO parameters){
		
		UserAppOutTO v_return=new UserAppOutTO();  
		
		System.out.println("Desde DAO");
		
		this.setDbObject("{? = call sp_valid_usr(?,?)}");
		//this.setString(1, "return");
		this.setString(2, "USRNAME",parameters.getIdUserApp());
		this.setString(3, "USRPSW", parameters.getPasswordUserApp());
		
		this.runQuery();
		System.out.println("return psg");
		System.out.println(this.getInt());
		v_return.setValidUser(this.getInt());
		//return this.getIntReturn();
		
		return v_return;
		
	}
	
	public ProfileOutTO getUserProfiles(ProfileInTO parameters){
		
		ProfileOutTO v_return=new ProfileOutTO();  
		
		System.out.println("Desde DAO");
		this.setTypeReturn(Common.TYPERETURN_RESULTSET);
		this.setDbObject("{call sp_get_usr_profiles(?)}");
		//this.setString(1, "return");
		this.setInt(1, "USRNAME",new Integer(1));
		 		
		this.runQuery();
		System.out.println("return psg");
		//System.out.println(this.getInt());
		 
		//return this.getIntReturn();
		
		return v_return;
		
	}

}
