package space.gorogoro.capsuletoy;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

/*
 * CapsuleToyUtility
 * @license    GPLv3
 * @copyright  Copyright gorogoro.space 2021
 * @author     kubotan
 * @see        <a href="https://gorogoro.space">gorogoro.space</a>
 */
public class CapsuleToyUtility {

  protected static final String NUMALPHA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  /**
   * Output stack trace to log file.
   * @param Exception Exception
   */
  public static void logStackTrace(Exception e){
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    pw.flush();
    Bukkit.getLogger().log(Level.WARNING, sw.toString());
  }

  /**
   * Determine whether punch is being processed.
   * @param Player Player
   * @return boolean true:That's right false:That's not it
   */
  public static boolean isInPunch(Player player){
    if( player.hasMetadata(CapsuleToyCommand.META_CHEST)){
      return true;
    }
    return false;
  }

  /**
   * get capsuletoy name in punch.
   * @param Player Player
   * @return String CapsuleToy name
   */
  public static String getcapsuleToyNameInPunch(Player player){
    for(MetadataValue mv:player.getMetadata(CapsuleToyCommand.META_CHEST)) {
      return mv.asString();
    }
    return null;
  }
  
  /**
   * Set punch processing.
   * @param Player Player
   * @param CapsuleToy capsuletoy
   * @param String CapsuleToy Name
   */
  public static void setPunch(Player player, CapsuleToy capsuletoy, String capsuleToyName){
    removePunch(player, capsuletoy);
    player.setMetadata(CapsuleToyCommand.META_CHEST, new FixedMetadataValue(capsuletoy, capsuleToyName));
  }

  /**
   * Remove punch processing.
   * @param Player Player
   * @param CapsuleToy capsuletoy
   */
  public static void removePunch(Player player, CapsuleToy capsuletoy){
    player.removeMetadata(CapsuleToyCommand.META_CHEST, capsuletoy);
  }
  
  /**
   * Send message to player
   * @param CommandSender CommandSender
   * @param String message
   */
  public static void sendMessage(CommandSender sender, String message){
    sender.sendMessage((Object)ChatColor.DARK_RED + "[CapsuleToy]" + " " + (Object)ChatColor.RED + message);
  }
  
  /**
   * Generate random code
   * @return String code
   */
  public static String generateCode(){
    Random random = new Random();
    int length1 = 4;
    StringBuilder randomString1 = new StringBuilder(length1);
    for (int i = 0; i < length1; i++) {
      int index = random.nextInt(NUMALPHA.length());
      randomString1.append(NUMALPHA.charAt(index));
    }
    int length2 = 6;
    StringBuilder randomString2 = new StringBuilder(length2);
    for (int i = 0; i < length2; i++) {
      int index = random.nextInt(NUMALPHA.length());
      randomString2.append(NUMALPHA.charAt(index));
    }
    int length3 = 4;
    StringBuilder randomString3 = new StringBuilder(length3);
    for (int i = 0; i < length3; i++) {
      int index = random.nextInt(NUMALPHA.length());
      randomString3.append(NUMALPHA.charAt(index));
    }

    return String.format("%s-%s-%s",
      randomString1,
      randomString2,
      randomString3
    );
  }

  public static ArrayList<Player> getTarget(Plugin capsuletoy, String selector) {
    return getTarget(capsuletoy, selector, null);
  }

  public static ArrayList<Player> getTarget(Plugin capsuletoy, String selector, CommandSender sender) {

    ArrayList<Player> list = new ArrayList<Player>();
    if(!selector.startsWith("@")) {
      Player p = capsuletoy.getServer().getPlayer(selector);
      if(p != null) {
        list.add(p);
      }
    } else {
      switch(selector) {
        case "@a":
          list.addAll(capsuletoy.getServer().getOnlinePlayers());
          break;

        case "@p":
          if((sender instanceof Player) || (sender instanceof BlockCommandSender)) {
            Location senderLocation = null;
            if(sender instanceof Player) {
              Player sp = (Player) sender;
              senderLocation = sp.getLocation();
            } else if(sender instanceof BlockCommandSender) {
              BlockCommandSender sb = (BlockCommandSender) sender;
              senderLocation = sb.getBlock().getLocation();
            }
            if(senderLocation != null) {
              list.add(getNearestPlayerByLocation(senderLocation));
            }
          }
          break;

        case "@s":
          if(sender instanceof Player) {
            list.add((Player) sender);
          }
          break;

        case "@r":
          ArrayList<Player> curPlayerList = new ArrayList<Player>();
          for (Player p : Bukkit.getOnlinePlayers()) {
            curPlayerList.add(p);
          }
          Random random = new Random();
          list.add(curPlayerList.get(random.nextInt(curPlayerList.size())));
          break;
      }
    }
    return list;
  }

  public static Player getNearestPlayerByLocation(Location l) {
    double lastDistance = Double.MAX_VALUE;
    Player latestPlayer = null;
    for(Player p : l.getWorld().getPlayers()) {
      double distance = p.getLocation().distance(l);
      if(distance < lastDistance) {
          lastDistance = distance;
          latestPlayer = p;
      }
    }
    return latestPlayer;
  }
}
