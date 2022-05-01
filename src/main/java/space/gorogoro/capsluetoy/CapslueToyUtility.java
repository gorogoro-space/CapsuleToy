package space.gorogoro.capsluetoy;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
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
 * CapslueToyUtility
 * @license    GPLv3
 * @copyright  Copyright gorogoro.space 2021
 * @author     kubotan
 * @see        <a href="https://gorogoro.space">gorogoro.space</a>
 */
public class CapslueToyUtility {

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
    if( player.hasMetadata(CapslueToyCommand.META_CHEST)){
      return true;
    }
    return false;
  }

  /**
   * get capsluetoy name in punch.
   * @param Player Player
   * @return String CapslueToy name
   */
  public static String getcapslueToyNameInPunch(Player player){
    for(MetadataValue mv:player.getMetadata(CapslueToyCommand.META_CHEST)) {
      return mv.asString();
    }
    return null;
  }
  
  /**
   * Set punch processing.
   * @param Player Player
   * @param CapslueToy capsluetoy
   * @param String CapslueToy Name
   */
  public static void setPunch(Player player, CapslueToy capsluetoy, String capslueToyName){
    removePunch(player, capsluetoy);
    player.setMetadata(CapslueToyCommand.META_CHEST, new FixedMetadataValue(capsluetoy, capslueToyName));
  }

  /**
   * Remove punch processing.
   * @param Player Player
   * @param CapslueToy capsluetoy
   */
  public static void removePunch(Player player, CapslueToy capsluetoy){
    player.removeMetadata(CapslueToyCommand.META_CHEST, capsluetoy);
  }
  
  /**
   * Send message to player
   * @param CommandSender CommandSender
   * @param String message
   */
  public static void sendMessage(CommandSender sender, String message){
    sender.sendMessage((Object)ChatColor.DARK_RED + "[CapslueToy]" + " " + (Object)ChatColor.RED + message);
  }
  
  /**
   * Generate random code
   * @return String code
   */
  public static String generateCode(){
    return String.format("%s-%s-%s",
      RandomStringUtils.random(4, NUMALPHA),
      RandomStringUtils.random(6, NUMALPHA),
      RandomStringUtils.random(4, NUMALPHA)
    );
  }

  /**
   * Scan Format
   * @param String format
   * @param String str
   * @return String value
   */
  public static String scanf(String format, String str) {
    return StringUtils.difference(format, str);
  }

  public static ArrayList<Player> getTarget(Plugin capsluetoy, String selector) {
    return getTarget(capsluetoy, selector, null);
  }

  public static ArrayList<Player> getTarget(Plugin capsluetoy, String selector, CommandSender sender) {

    ArrayList<Player> list = new ArrayList<Player>();
    if(!selector.startsWith("@")) {
      Player p = capsluetoy.getServer().getPlayer(selector);
      if(p != null) {
        list.add(p);
      }
    } else {
      switch(selector) {
        case "@a":
          list.addAll(capsluetoy.getServer().getOnlinePlayers());
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
