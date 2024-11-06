package space.gorogoro.capsuletoy;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;

/*
 * CapsuleToyDatabase
 * @license    GPLv3
 * @copyright  Copyright gorogoro.space 2021
 * @author     kubotan
 * @see        <a href="https://gorogoro.space">gorogoro.space</a>
 */
public class CapsuleToyDatabase {
  private CapsuleToy capsuletoy;
  private Connection con;
  private List<String> listCapsuleToySignCache = new ArrayList<String>();
  private long expireCache; 
  
  /**
   * Constructor of CapsuleToyDatabase.
   * @param CapsuleToy capsuletoy
   */
  public CapsuleToyDatabase(CapsuleToy capsuletoy) {
    this.capsuletoy = capsuletoy;
  }

  /**
   * Get connection.
   * @return Connection Connection
   */
  private Connection getCon(){
    try{
      // Create database folder.
      if(!capsuletoy.getDataFolder().exists()){
        capsuletoy.getDataFolder().mkdir();
      }
      if(con == null) {
        // Select JDBC driver.
        Class.forName("org.sqlite.JDBC");
        String url = "jdbc:sqlite:" + capsuletoy.getDataFolder() + File.separator + "sqlite.db";
        con = DriverManager.getConnection(url);
        con.setAutoCommit(true);
      }
    } catch (Exception e){
      CapsuleToyUtility.logStackTrace(e);
      closeCon(con);
    }
    return con;
  }

  /**
   * Get statement.
   * @return Statement Statement
   */
  private Statement getStmt(){
    Statement stmt = null;
    try{
      if(stmt == null) {
        stmt = getCon().createStatement();
        stmt.setQueryTimeout(capsuletoy.getConfig().getInt("query-timeout"));
      }
    } catch (Exception e){
      CapsuleToyUtility.logStackTrace(e);
    }
    return stmt;
  }

  /**
   * Close connection.
   * @param Connection Connection
   */
  private static void closeCon(Connection con){
    try{
      if(con != null){
        con.close();
      }
    } catch (Exception e){
      CapsuleToyUtility.logStackTrace(e);
    }
  }

  /**
   * Close result set.
   * @param ResultSet Result set
   */
  private static void closeRs(ResultSet rs) {
    try{
      if(rs != null){
        rs.close();
      }
    } catch (Exception e){
      CapsuleToyUtility.logStackTrace(e);
    }
  }

  /**
   * Close statement.
   * @param Statement Statement
   */
  private static void closeStmt(Statement stmt) {
    try{
      if(stmt != null){
        stmt.close();
      }
    } catch (Exception e){
      CapsuleToyUtility.logStackTrace(e);
    }
  }

  /**
   * Close prepared statement.
   * @param PreparedStatement PreparedStatement
   */
  private static void closePrepStmt(PreparedStatement prepStmt){
    try{
      if(prepStmt != null){
        prepStmt.close();
      }
    } catch (Exception e){
      CapsuleToyUtility.logStackTrace(e);
    }
  }

  /**
   * Finalize
   */
  public void finalize() {
    try{
      closeCon(getCon());
      listCapsuleToySignCache  = new ArrayList<String>();
      expireCache = System.currentTimeMillis();
    } catch (Exception e){
      CapsuleToyUtility.logStackTrace(e);
    }
  }

  /**
   * Initialize
   */
  public void initialize() {
    ResultSet rs = null;
    Statement stmt = null;
    try{
      stmt = getStmt();
      
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS capsuletoy ("
        + "  id INTEGER PRIMARY KEY AUTOINCREMENT"
        + "  ,capsuletoy_name STRING NOT NULL"
        + "  ,capsuletoy_display_name STRING NOT NULL"
        + "  ,capsuletoy_detail STRING NOT NULL"
        + "  ,world_name STRING NOT NULL"
        + "  ,sign_x INTEGER NOT NULL"
        + "  ,sign_y INTEGER NOT NULL"
        + "  ,sign_z INTEGER NOT NULL"
        + "  ,chest_x INTEGER NOT NULL DEFAULT 0"
        + "  ,chest_y INTEGER NOT NULL DEFAULT 0"
        + "  ,chest_z INTEGER NOT NULL DEFAULT 0"
        + "  ,updated_at DATETIME NOT NULL DEFAULT (datetime('now','localtime')) CHECK(updated_at LIKE '____-__-__ __:__:__')"
        + "  ,created_at DATETIME NOT NULL DEFAULT (datetime('now','localtime')) CHECK(created_at LIKE '____-__-__ __:__:__')"
        + ");"
      );
      stmt.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS capsuletoy_name_uindex ON capsuletoy (capsuletoy_name);");
      
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticket ("
        + "  id INTEGER PRIMARY KEY AUTOINCREMENT"
        + "  ,ticket_code STRING NOT NULL"
        + "  ,created_at DATETIME NOT NULL DEFAULT (datetime('now','localtime')) CHECK(created_at LIKE '____-__-__ __:__:__')"
        + ");"
      );
      stmt.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS ticket_code_uindex ON ticket (ticket_code);");
    
      closeStmt(stmt);

      refreshCache();

    } catch (Exception e){
      CapsuleToyUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closeStmt(stmt);
    }
  }
  
  /**
   * list
   * @return ArrayList
   */
  public List<String> list(){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<String> ret = new ArrayList<String>();
    try {
      prepStmt = getCon().prepareStatement("SELECT"
        + "  capsuletoy_name "
        + "  ,world_name "
        + "  ,sign_x "
        + "  ,sign_y "
        + "  ,sign_z "
        + "  ,chest_x "
        + "  ,chest_y "
        + "  ,chest_z "
        + "FROM"
        + "  capsuletoy "
        + "ORDER BY"
        + "  id DESC"
        );
      rs = prepStmt.executeQuery();
      while(rs.next()){
        ret.add(
          String.format(
            "capsuletoy_name:%s world:%s sign[x,y,z]:%d,%d,%d chest[x,y,z]:%d,%d,%d"
            ,rs.getString(1)
            ,rs.getString(2)
            ,rs.getInt(3)
            ,rs.getInt(4)
            ,rs.getInt(5)
            ,rs.getInt(6)
            ,rs.getInt(7)
            ,rs.getInt(8)
          )
        );
      }
      closeRs(rs);
      closePrepStmt(prepStmt);
    } catch (SQLException e) {
      CapsuleToyUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
    return ret;
  }
  
  /**
   * Delete capsuletoy
   * @param String capsuleToyName
   * @return boolean true:Success false:Failure
   */
  public boolean deleteCapsuleToy(String capsuleToyName) {
    PreparedStatement prepStmt = null;
    try {
      prepStmt = getCon().prepareStatement("DELETE FROM capsuletoy WHERE capsuletoy_name = ?;");
      prepStmt.setString(1, capsuleToyName);
      prepStmt.addBatch();
      prepStmt.executeBatch();
      closePrepStmt(prepStmt);
      refreshCache();

    } catch (SQLException e) {
      CapsuleToyUtility.logStackTrace(e);
      return false;
    }
    return true;
  }

  /**
   * Get capsuletoy id
   * @param String capsuleToyName
   * @return Integer|null CapsuleToy id.
   */
  public Integer getCapsuleToy(String capsuleToyName){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Integer capsuleToyId = null;
    try {
      prepStmt = getCon().prepareStatement("SELECT id FROM capsuletoy WHERE capsuletoy_name=?");
      prepStmt.setString(1, capsuleToyName);
      rs = prepStmt.executeQuery();
      while(rs.next()){
        capsuleToyId = rs.getInt(1);
      }
      closeRs(rs);
      closePrepStmt(prepStmt);
    } catch (SQLException e) {
      CapsuleToyUtility.logStackTrace(e);
    }
    return capsuleToyId;
  }

  /**
   * Get capsuletoy chest
   * @param Location signLoc
   * @return Chest|null CapsuleToy chest.
   */
  public Chest getCapsuleToyChest(Location signLoc){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = getCon().prepareStatement("SELECT world_name, chest_x, chest_y, chest_z FROM capsuletoy WHERE world_name=? AND sign_x=? AND sign_y=? AND sign_z=?");
      prepStmt.setString(1, signLoc.getWorld().getName());
      prepStmt.setInt(2, signLoc.getBlockX());
      prepStmt.setInt(3, signLoc.getBlockY());
      prepStmt.setInt(4, signLoc.getBlockZ());
      rs = prepStmt.executeQuery();
      String worldName = "";
      Integer chestX = null;
      Integer chestY = null;
      Integer chestZ = null;
      boolean isChestNothing = false;
      while(rs.next()){
        worldName = rs.getString(1);
        chestX = rs.getInt(2);
        if (rs.wasNull()) {
          isChestNothing = true;
        }
        chestY = rs.getInt(3);
        if (rs.wasNull()) {
          isChestNothing = true;
        }
        chestZ = rs.getInt(4);
        if (rs.wasNull()) {
          isChestNothing = true;
        }
      }
      closeRs(rs);
      closePrepStmt(prepStmt);
      
      if(isChestNothing) {
        return null;
      }
      
      Block b = new Location(capsuletoy.getServer().getWorld(worldName), chestX, chestY, chestZ).getBlock();
      if(!b.getType().equals(Material.CHEST)) {
        return null;
      }
      
      return (Chest)b.getState();
    } catch (SQLException e) {
      CapsuleToyUtility.logStackTrace(e);
    }
    return null;
  }

  /**
   * Get capsuletoy id
   * @param Location loc
   * @return Integer|null CapsuleToy id.
   */
  public boolean isCapsuleToy(Location loc){
    try {
      boolean cacheClear = false;
      if(expireCache > System.currentTimeMillis()) {
        cacheClear = true;
      }
      return isCapsuleToy(loc, cacheClear);
    } catch (Exception e) {
      CapsuleToyUtility.logStackTrace(e);
    }
    return false;
  }
  
  /**
   * Get capsuletoy id
   * @param Location loc
   * @param boolean cacheClear
   * @return Integer|null CapsuleToy id.
   */
  public boolean isCapsuleToy(Location loc, boolean cacheClear){
    try {
      if( cacheClear ) {
        refreshCache();
      }
      String searchIndex = String.join(
        "_"
        ,loc.getWorld().getName()
        ,String.valueOf(loc.getBlockX())
        ,String.valueOf(loc.getBlockY())
        ,String.valueOf(loc.getBlockZ())
      );
      if(listCapsuleToySignCache.contains(searchIndex)) {
        // no database & cache hit.
        return true;
      }

    } catch (Exception e) {
      CapsuleToyUtility.logStackTrace(e);
    }
    return false;
  }
  
  /**
   * Refresh Cache
   * @return boolean Success:true Failure:false
   */
  public boolean refreshCache(){
    try {
      PreparedStatement prepStmt = getCon().prepareStatement("SELECT world_name, sign_x, sign_y, sign_z FROM capsuletoy");
      ResultSet rs = prepStmt.executeQuery();
      String cacheIndex;
      listCapsuleToySignCache.clear();
      while(rs.next()){
        cacheIndex = String.join(
          "_"
          ,rs.getString(1)
          ,String.valueOf(rs.getInt(2))
          ,String.valueOf(rs.getInt(3))
          ,String.valueOf(rs.getInt(4))
        );
        listCapsuleToySignCache.add(cacheIndex);
      }
      expireCache = System.currentTimeMillis() + (capsuletoy.getConfig().getInt("cache-expire-seconds") * 1000);
      closeRs(rs);
      closePrepStmt(prepStmt);
      return true;
    } catch (SQLException e) {
      CapsuleToyUtility.logStackTrace(e);
    }
    return false;
  }

  /**
   * Get capsuletoy
   * @param String capsuleToyName
   * @param String capsuletoyDisplayNam
   * @param String capsuleToyDetail
   * @param Integer worldId
   * @param Integer sign_x
   * @param Integer sign_y
   * @param Integer sign_z
   * @return Integer CapsuleToy id.
   */
  public Integer getCapsuleToy(String capsuleToyName, String capsuleToyDisplayName, String capsuleToyDetail, String worldName, Integer signX, Integer signY, Integer signZ){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Integer capsuleToyId = null;
    try {

      capsuleToyId = getCapsuleToy(capsuleToyName);
      if(capsuleToyId != null){
        return capsuleToyId;
      }
      
      prepStmt = getCon().prepareStatement("INSERT INTO capsuletoy("
        + "  capsuletoy_name"
        + ", capsuletoy_display_name"
        + ", capsuletoy_detail"
        + ", world_name"
        + ", sign_x"
        + ", sign_y"
        + ", sign_z"
        + ") VALUES (?,?,?,?,?,?,?)");
      prepStmt.setString(1, capsuleToyName);
      prepStmt.setString(2, capsuleToyDisplayName);
      prepStmt.setString(3, capsuleToyDetail);
      prepStmt.setString(4, worldName);
      prepStmt.setInt(5, signX);
      prepStmt.setInt(6, signY);
      prepStmt.setInt(7, signZ);
      // Bukkit.getServer().getLogger().warning(prepStmt.toString());
      prepStmt.addBatch();
      prepStmt.executeBatch();
      rs = prepStmt.getGeneratedKeys();
      if (rs.next()) {
        capsuleToyId = rs.getInt(1);
      }
      closeRs(rs);
      closePrepStmt(prepStmt);
      refreshCache();

    } catch (SQLException e) {
      CapsuleToyUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
    return capsuleToyId;
  }

  /**
   * Update capsuletoy chest
   * @param capsuleToyName
   * @param Integer chestX
   * @param Integer chestY
   * @param Integer chestZ
   * @return Integer CapsuleToy id.
   */
  public boolean updateCapsuleToyChest(String capsuleToyName, Integer chestX, Integer chestY, Integer chestZ){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      Integer capsuleToyId = getCapsuleToy(capsuleToyName);
      if(capsuleToyId == null){
        return false;
      }
      
      prepStmt = getCon().prepareStatement("UPDATE capsuletoy SET chest_x = ?, chest_y = ?, chest_z = ? WHERE capsuletoy_name = ?;");
      prepStmt.setInt(1, chestX);
      prepStmt.setInt(2, chestY);
      prepStmt.setInt(3, chestZ);
      prepStmt.setString(4, capsuleToyName);
      prepStmt.addBatch();
      prepStmt.executeBatch();
      closePrepStmt(prepStmt);
      return true;
    } catch (SQLException e) {
      CapsuleToyUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
    return false;
  }
  
  /**
   * Delete ticket
   * @param String ticketCode
   * @return boolean true:Success false:Failure
   */
  public boolean deleteTicket(String ticketCode) {
    PreparedStatement prepStmt = null;
    try {
      prepStmt = getCon().prepareStatement("DELETE FROM ticket WHERE ticket_code = ?;");
      prepStmt.setString(1, ticketCode);
      prepStmt.addBatch();
      prepStmt.executeBatch();
      closePrepStmt(prepStmt);

    } catch (SQLException e) {
      CapsuleToyUtility.logStackTrace(e);
      return false;
    }
    return true;
  }

  /**
   * Issue a ticket
   * @param Player player
   * @return String|null ticketCode
   */
  public String getTicket() {
    return getTicket(0);
  }
  
  /**
   * Issue a ticket
   * @param Player player
   * @param Integer countRetry
   * @return String|null ticketCode
   */
  private String getTicket(Integer countRetry) {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String ticketCode = null;
    try {
      if(countRetry > 3) {
        return null;
      }
      countRetry++;      
      String curTicketCode = CapsuleToyUtility.generateCode();
      if(existsTicket(curTicketCode)){
        return getTicket(countRetry);
      }

      prepStmt = getCon().prepareStatement("INSERT INTO ticket(ticket_code) VALUES (?);");
      prepStmt.setString(1, curTicketCode);
      prepStmt.addBatch();
      prepStmt.executeBatch();
      closeRs(rs);
      closePrepStmt(prepStmt);
      ticketCode = curTicketCode;
      
    } catch (SQLException e) {
      CapsuleToyUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
    return ticketCode;
  }
  
  /**
   * Exists ticket
   * @param String ticketCode
   * @return boolean true:found ticket code. false:not found ticket code.
   */
  public boolean existsTicket(String ticketCode) {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = getCon().prepareStatement("SELECT id FROM ticket WHERE ticket_code = ?;");
      prepStmt.setString(1, ticketCode);
      rs = prepStmt.executeQuery();
      while(rs.next()){
        closeRs(rs);
        closePrepStmt(prepStmt);
        return true;
      }
      closeRs(rs);
      closePrepStmt(prepStmt);
    } catch (SQLException e) {
      CapsuleToyUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
    return false;
  }
}
