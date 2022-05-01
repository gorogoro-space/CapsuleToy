package space.gorogoro.capsluetoy;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/*
 * CapslueToyCommand
 * @license    GPLv3
 * @copyright  Copyright gorogoro.space 2021
 * @author     kubotan
 * @see        <a href="https://gorogoro.space">gorogoro.space</a>
 */
public class CapslueToyCommand {
  private CapslueToy capsluetoy;
  private CommandSender sender;
  private String[] args;
  protected static final String META_CHEST = "capsluetoy.chest";
  protected static final String FORMAT_TICKET_CODE = "CAPSLUETOY CODE:%s";

  /**
   * Constructor of CapslueToyCommand.
   * @param CapslueToy capsluetoy
   */
  public CapslueToyCommand(CapslueToy capsluetoy) {
    try{
      this.capsluetoy = capsluetoy;
    } catch (Exception e){
      CapslueToyUtility.logStackTrace(e);
    }
  }

  /**
   * Initialize
   * @param CommandSender CommandSender
   * @param String[] Argument
   */
  public void initialize(CommandSender sender, String[] args){
    try{
      this.sender = sender;
      this.args = args;
    } catch (Exception e){
      CapslueToyUtility.logStackTrace(e);
    }
  }

  /**
   * Finalize
   */
  public void finalize() {
    try{
      this.sender = null;
      this.args = null;
    } catch (Exception e){
      CapslueToyUtility.logStackTrace(e);
    }
  }

  /**
   * Processing of command list.
   * @return boolean true:Success false:Failure
   */
  public boolean list() {
    List<String> glist = capsluetoy.getDatabase().list();
    if(glist.size() <= 0) {
      CapslueToyUtility.sendMessage(sender, "Record not found.");
      return true;
    }
    
    for(String msg: glist) {
      CapslueToyUtility.sendMessage(sender, msg);
    }
    return true;
  }

  /**
   * Processing of command modify.
   * @return boolean true:Success false:Failure
   */
  public boolean modify() {
    if(args.length != 2) {
      return false;
    }
    
    if(!(sender instanceof Player)) {
      return false;
    }
    
    String capslueToyName = args[1];
    if(capsluetoy.getDatabase().getCapslueToy(capslueToyName) == null) {
      CapslueToyUtility.sendMessage(sender, "Record not found. capsluetoy_name=" + capslueToyName);
      return true;
    }
    CapslueToyUtility.setPunch((Player)sender, capsluetoy, capslueToyName);
    CapslueToyUtility.sendMessage(sender, "Please punching(right click) a chest of capsluetoy. capsluetoy_name=" + capslueToyName);
    return true;
  }
  
  /**
   * Processing of command delete.
   * @return boolean true:Success false:Failure
   */
  public boolean delete() {
    if(args.length != 2) {
      return false;
    }
    
    String capslueToyName = args[1];
    if(capsluetoy.getDatabase().deleteCapslueToy(capslueToyName)) {
      CapslueToyUtility.sendMessage(sender, "Deleted. capsluetoy_name=" + capslueToyName);
      return true;
    }
    return false;
  }

  /**
   * Processing of command ticket.
   * @return boolean true:Success false:Failure
   */
  public boolean ticket(Player p) {
    if(args.length != 2) {
      return false;
    }

    int emptySlot = p.getInventory().firstEmpty();
    if (emptySlot == -1) {
      // not empty
      return false;
    }

    String ticketCode = capsluetoy.getDatabase().getTicket();
    if(ticketCode == null) {
      CapslueToyUtility.sendMessage(sender, "Failure generate ticket code.");
      return false;
    }

    ItemStack ticket = new ItemStack(Material.PAPER, 1);
    ItemMeta im = ticket.getItemMeta();
    im.setDisplayName(ChatColor.translateAlternateColorCodes('&', capsluetoy.getConfig().getString("ticket-display-name")));
    ArrayList<String> lore = new ArrayList<String>();
    lore.add(ChatColor.translateAlternateColorCodes('&', capsluetoy.getConfig().getString("ticket-lore1")));
    lore.add(ChatColor.translateAlternateColorCodes('&', capsluetoy.getConfig().getString("ticket-lore2")));
    lore.add(String.format(FORMAT_TICKET_CODE, ticketCode));
    im.setLore(lore);
    ticket.setItemMeta(im);
    p.getInventory().setItem(emptySlot, ticket);
    
    CapslueToyUtility.sendMessage(sender, "Issue a ticket. player_name=" + p.getDisplayName());
    return true;
  }

  /**
   * Processing of command reload.
   * @return boolean true:Success false:Failure
   */
  public boolean reload() {
    capsluetoy.reloadConfig();
    CapslueToyUtility.sendMessage(sender, "reloaded.");
    return true;
  }

  /**
   * Processing of command enable.
   * @return boolean true:Success false:Failure
   */
  public boolean enable() {
    capsluetoy.onEnable();
    CapslueToyUtility.sendMessage(sender, "enabled.");
    return true;
  }

  /**
   * Processing of command fgdisable.
   * @return boolean true:Success false:Failure
   */
  public boolean disable() {
    capsluetoy.onDisable();
    CapslueToyUtility.sendMessage(sender, "disabled.");
    return true;
  }
}
