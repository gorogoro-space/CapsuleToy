package space.gorogoro.capsuletoy;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/*
 * CapsuleToyCommand
 * @license    GPLv3
 * @copyright  Copyright gorogoro.space 2021
 * @author     kubotan
 * @see        <a href="https://gorogoro.space">gorogoro.space</a>
 */
public class CapsuleToyCommand {
  private CapsuleToy capsuletoy;
  private CommandSender sender;
  private String[] args;
  protected static final String META_CHEST = "capsuletoy.chest";
  protected static final String PREFIX_TICKET_CODE = "CAPSULETOY CODE:";

  /**
   * Constructor of CapsuleToyCommand.
   * @param CapsuleToy capsuletoy
   */
  public CapsuleToyCommand(CapsuleToy capsuletoy) {
    try{
      this.capsuletoy = capsuletoy;
    } catch (Exception e){
      CapsuleToyUtility.logStackTrace(e);
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
      CapsuleToyUtility.logStackTrace(e);
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
      CapsuleToyUtility.logStackTrace(e);
    }
  }

  /**
   * Processing of command list.
   * @return boolean true:Success false:Failure
   */
  public boolean list() {
    List<String> glist = capsuletoy.getDatabase().list();
    if(glist.size() <= 0) {
      CapsuleToyUtility.sendMessage(sender, "Record not found.");
      return true;
    }
    
    for(String msg: glist) {
      CapsuleToyUtility.sendMessage(sender, msg);
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
    
    String capsuleToyName = args[1];
    if(capsuletoy.getDatabase().getCapsuleToy(capsuleToyName) == null) {
      CapsuleToyUtility.sendMessage(sender, "Record not found. capsuletoy_name=" + capsuleToyName);
      return true;
    }
    CapsuleToyUtility.setPunch((Player)sender, capsuletoy, capsuleToyName);
    CapsuleToyUtility.sendMessage(sender, "Please punching(right click) a chest of capsuletoy. capsuletoy_name=" + capsuleToyName);
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
    
    String capsuleToyName = args[1];
    if(capsuletoy.getDatabase().deleteCapsuleToy(capsuleToyName)) {
      CapsuleToyUtility.sendMessage(sender, "Deleted. capsuletoy_name=" + capsuleToyName);
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

    String ticketCode = capsuletoy.getDatabase().getTicket();
    if(ticketCode == null) {
      CapsuleToyUtility.sendMessage(sender, "Failure generate ticket code.");
      return false;
    }

    ItemStack ticket = new ItemStack(Material.PAPER, 1);
    ItemMeta im = ticket.getItemMeta();
    im.setDisplayName(ChatColor.translateAlternateColorCodes('&', capsuletoy.getConfig().getString("ticket-display-name")));
    ArrayList<String> lore = new ArrayList<String>();
    lore.add(ChatColor.translateAlternateColorCodes('&', capsuletoy.getConfig().getString("ticket-lore1")));
    lore.add(ChatColor.translateAlternateColorCodes('&', capsuletoy.getConfig().getString("ticket-lore2")));
    lore.add(String.format(PREFIX_TICKET_CODE+"%s", ticketCode));
    im.setLore(lore);
    ticket.setItemMeta(im);
    p.getInventory().setItem(emptySlot, ticket);
    
    CapsuleToyUtility.sendMessage(sender, "Issue a ticket. player_name=" + p.getDisplayName());
    return true;
  }

  /**
   * Processing of command reload.
   * @return boolean true:Success false:Failure
   */
  public boolean reload() {
    capsuletoy.reloadConfig();
    CapsuleToyUtility.sendMessage(sender, "reloaded.");
    return true;
  }

  /**
   * Processing of command enable.
   * @return boolean true:Success false:Failure
   */
  public boolean enable() {
    capsuletoy.onEnable();
    CapsuleToyUtility.sendMessage(sender, "enabled.");
    return true;
  }

  /**
   * Processing of command fgdisable.
   * @return boolean true:Success false:Failure
   */
  public boolean disable() {
    capsuletoy.onDisable();
    CapsuleToyUtility.sendMessage(sender, "disabled.");
    return true;
  }
}
