package com.eb.data.database.CRUD;

	import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import snaq.db.ConnectionPoolManager;
	 
	/**
	 * DBPool class file will be used to get new Database connection with the help of ConnectionPoolManager. 
	 
	 * It will fetch all the DB Connection properties from DBPool.properties file and it will create new connection.
	 */
	public class DBPoolUtil {
	  
	 protected Connection conn;  
	    protected ConnectionPoolManager connManager;
	    private static DBPoolUtil dbPool;
	     
	    static Logger logger = Logger.getLogger(DBPoolUtil.class);
	     
	    // Name of the database connection name from DBPool.properties file.
	    static final String databaseName = "inventory";
	     
	    /** 
	     * Class constructor creates ConnectionPoolManager object
	     * @exception properties file not found.
	     */
	    public DBPoolUtil(){
	     try {  
	            connManager = ConnectionPoolManager.getInstance("DBPool.properties");  
	        } catch (IOException ex) {  
	         logger.info("Error While Connecting with DBPool Properties file :=> "+ex.toString()); 
	        }  
	    }
	     
	     
	    /**
	     * Creates/Provides the instance of the Pool.
	     * @return DBPool
	     */
	    public static DBPoolUtil getInstance(){
	     if(dbPool==null)
	      dbPool = new DBPoolUtil();
	     return dbPool;
	    }
	     
	    /**
	     * Sets the connection object.
	     * @exception cannot get connection.
	     */
	    public Connection getConn() {  
	        Connection con = null;  
	        try {  
	            con = connManager.getConnection(databaseName);
	            logger.info("Connection Created: " + con.toString());  
	        } catch (SQLException ex) {  
	         logger.info("Error While Creating Connection :=> "+ex.toString());  
	        }  
	        if (con != null) {  
	            this.conn = con;  
	            logger.info("Connection Released: "+ this.conn.toString());  
	            return con;  
	        } else {  
	            return con;  
	        }  
	    } 
	}
