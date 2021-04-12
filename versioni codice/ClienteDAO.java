package model;
import java.sql.*;
import java.util.ArrayList;


public class ClienteDAO {
	private static AbstractFactory factory = new FactoryElementoUtente();
	private static Connector connector = Connector.getConnectorInstance();
	
	
	public static void clientPersistence(ArrayList<Cliente> clienti) {
		
		
		 Connection conn= connector.getConnection();
		 Statement stmt = null;
		 
		 try {
		    for (Cliente c : clienti) {

		    	int punti = c.getPunti();
		    	int livello = c.getLivello();
			 
		    	String sql = "UPDATE `Cliente` SET `livello` = '"+ livello + "' , `punti` = '" + punti + "' WHERE `username` = '" + c.getUsername() + "';";
			
		    	stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

		    	stmt.executeUpdate(sql);
		    }
		    	
		    } catch (SQLException se) {
		        // Errore durante l'apertura della connessione
		        se.printStackTrace();
		    } catch (Exception e) {
		        // Errore nel loading del driver
		        e.printStackTrace();
		    } finally {
		        try {
		            if (stmt != null)
		                stmt.close();
		   
		        } catch (SQLException se2) {
		        }
		        try {
		            if (conn != null)
		                conn.close();
		        } catch (SQLException se) {
		            se.printStackTrace();
		        }
		    }
		
		
	}
	
	
	public static Cliente creaUtenteCliente(String username, String pwd, String email, String farma) {
		Statement stmt = null;
		Statement stmt2 = null;
		Statement stmt3 = null;
		Statement stmt4 = null;
		 Connection conn= connector.getConnection();
        //Connection conn = null;
        Cliente c = null;
        
        try {
        	
        	String sql = "SELECT `username` " + "FROM `Utenti` where `username` = '" + username + "';";
            String sql2 = "INSERT INTO `Utenti` (`username`, `password`, `email`) values ('"+ username + "', '" + pwd + "', '" +email + "');" ;
            String sql3 = "SELECT `username` " + "FROM `Farmacia` where `username` = '" + farma + "';";
            String sql4 = "INSERT INTO `Cliente` (`username`, `farmacia associata`) values ('"+ username + "', '" + farma + "');" ;
            
        	
        	/*Class.forName(DRIVER_CLASS_NAME);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);*/
            
          
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt2 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt3 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt4 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            
            
            ResultSet rs = stmt.executeQuery(sql);
            ResultSet rs3 = stmt3.executeQuery(sql3);
            
            if (!rs3.first()) return null;			//se rs3 non trova farmacie l'input era errato
            
            if (!rs.first()) {						// rs vuoto --> posso procedere
            	
            	
            	stmt2.executeUpdate(sql2);
            	stmt4.executeUpdate(sql4);
            	c = new Cliente(username, pwd, email);
            	c.setFarmaciaAssociata(farma);
            	return c;

            }
            rs.close();
            stmt.close();
            
            conn.close();
        } catch (SQLException se) {
            // Errore durante l'apertura della connessione
            se.printStackTrace();
        } catch (Exception e) {
            // Errore nel loading del driver
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return null;
	}
	
	
	public static ArrayList<Cliente> customerOfThisPharmacy(String farmaciaUsername){
		 ArrayList <Cliente> clienti = new ArrayList<Cliente>();
		 Connection conn= connector.getConnection();

		 Statement stmt = null;
		
	 
		 try {
	    	
	    	String sql = "SELECT U.`username`, U.`password`, U.`email`, C.`punti`, C.`livello` FROM `Cliente` C join `Utenti` U on C.`username` = U.`username` WHERE  C.`farmacia associata`='" + farmaciaUsername + "';";
	    	stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	        ResultSet rs = stmt.executeQuery(sql);
	        
	        
	        if (rs.first()) {
	        	rs.first();
	        	do {
	        	
	        	  		Cliente cl = (Cliente) factory.creaUtente(rs.getString("username"), rs.getString("password"), rs.getString("email"));
	        	  		cl.setFarma_associata(farmaciaUsername);
	        	  		cl.setPunti(rs.getInt("punti"));
	        	  		cl.setLivello(rs.getInt("livello"));
	              
	              	
	        	  		ArrayList<FarmacoCliente> farmaci = FarmacoClienteDAO.myFarmaciCliente(rs.getString("username"));
	        	  		ArrayList <EventoCliente> eventi = EventoDAO.allMyEvents(rs.getString("username"));

	        	  		cl.setFarmaci(farmaci);
	        	  		cl.setEventi(eventi);
	          		
	        	  		clienti.add(cl);
	        	  		
	        	  	} while(rs.next());
	        	
	        }
	        rs.close();

	        
	    
	    } catch (SQLException se) {
	        // Errore durante l'apertura della connessione
	        se.printStackTrace();
	    } catch (Exception e) {
	        // Errore nel loading del driver
	        e.printStackTrace();
	    } finally {
	        try {
	            if (stmt != null)
	                stmt.close();
	   
	        } catch (SQLException se2) {
	        }
	        try {
	            if (conn != null)
	                conn.close();
	        } catch (SQLException se) {
	            se.printStackTrace();
	        }
	    }
	    return clienti;
	}
	
	
	
	
	
	
	public static  Cliente esisteCliente(String username, String pwd) {
		
		Statement stmt = null;
		Statement stmt2 = null;
		Statement stmt3 = null;
		Connection conn= connector.getConnection();
        Cliente cl = null;
        
        ArrayList<FarmacoCliente> farmaci = null;
        ArrayList <EventoCliente> eventi = null;
        //ArrayList <Evento> eventiFarmaciaAssociata = null;
        
        try {
        	
        	String sql = "SELECT C.`username`, C.`farmacia associata`, C.`punti`, C.`livello`, U.`password`, U.`email` " + "FROM `Cliente` C join `Utenti` U  on C.`username` = U.`username` WHERE C.`username` = '" + username + "' and U.`password` = '" + pwd + "';";
        	
        
        	/*Class.forName(DRIVER_CLASS_NAME);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);*/
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt2 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            //stmt3 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            
   
            ResultSet rs = stmt.executeQuery(sql);
   
   
            if	(rs.first()) {
            	rs.first();
            	cl = (Cliente)factory.creaUtente(rs.getString("username"), rs.getString("password"), rs.getString("email"));
            	cl.setFarma_associata(rs.getString("farmacia associata"));
            	cl.setPunti(rs.getInt("punti"));
            	cl.setLivello(rs.getInt("livello"));
            
            	rs.close();
            
            	farmaci = FarmacoClienteDAO.myFarmaciCliente(username);
            	eventi = EventoDAO.allMyEvents(username);
            	//eventiFarmaciaAssociata = EventoDAO.allEventsFarmacia(cl.getFarma_associata());
        	
        	
        	cl.setFarmaci(farmaci);
        	cl.setEventi(eventi);
            }
        } catch (SQLException se) {
            // Errore durante l'apertura della connessione
            se.printStackTrace();
        } catch (Exception e) {
            // Errore nel loading del driver
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (stmt2 != null)
                    stmt2.close();
                if (stmt3 != null)
                    stmt3.close();
            } catch (SQLException se2) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return cl;
	}
	
		
public static  ArrayList<String> datiAccount(String username) {
		
		Statement stmt = null;
		Connection conn= connector.getConnection();
        
        ArrayList<String> dati = new ArrayList<String>();
        try {
        	
        	String sql = "select C.`username`, C.`farmacia associata`, C.`punti`, C.`livello`, U.`email` from `Cliente` C join `Utenti` U on C.`username` = U.`username` where C.`username` = '" + username + "';";
        	
        	/*Class.forName(DRIVER_CLASS_NAME);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);*/
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery(sql);
            
           
            
            if	(rs.first()) {
            	rs.first();
            	dati.add(rs.getString("username"));
            	dati.add(rs.getString("farmacia associata"));
            	dati.add(Integer.toString(rs.getInt("punti")));
            	dati.add(Integer.toString(rs.getInt("livello")));
            	dati.add(rs.getString("email"));
            }
          
            rs.close();
        	
        } catch (SQLException se) {
            // Errore durante l'apertura della connessione
            se.printStackTrace();
        } catch (Exception e) {
            // Errore nel loading del driver
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return dati;
	}
	
	
}
