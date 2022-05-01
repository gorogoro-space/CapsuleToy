package space.gorogoro.capsluetoy;

import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/*
 * CapslueToyListener
 * @license    GPLv3
 * @copyright  Copyright gorogoro.space 2021
 * @author     kubotan
 * @see        <a href="https://gorogoro.space">gorogoro.space</a>
 */
public class CapslueToyListener implements Listener{
  private CapslueToy capsluetoy;

  /**
   * Constructor of CapslueToyListener.
   */
  public CapslueToyListener(CapslueToy capsluetoy) {
    try{
      this.capsluetoy = capsluetoy;
    } catch (Exception e){
      CapslueToyUtility.logStackTrace(e);
    }
  }

  /**
   * On sign change
   * @param SignChangeEvent event
   */
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onSignChange(SignChangeEvent event) {
    try {
      if(!event.getLine(0).toLowerCase().equals("[capsluetoy]")) {
        return;
      }

      if(!event.getPlayer().hasPermission("capsluetoy.create")) {
        return;
      }

      Location signLoc = event.getBlock().getLocation();
      if(capsluetoy.getDatabase().isCapslueToy(signLoc)) {
        event.setCancelled(true);
        CapslueToyUtility.sendMessage(event.getPlayer(), "It is already registered. To continue, please delete first.");
        return;
      }

      String capslueToyName = event.getLine(1);
      String capslueToyDisplayName = event.getLine(2);
      String capslueToyDetail = event.getLine(3);
      String worldName = signLoc.getWorld().getName();
      Integer x = signLoc.getBlockX();
      Integer y = signLoc.getBlockY();
      Integer z = signLoc.getBlockZ();
      Pattern p = Pattern.compile("^[0-9a-zA-Z_]+$");
      if(!p.matcher(capslueToyName).find()) {
        event.setCancelled(true);
        CapslueToyUtility.sendMessage(event.getPlayer(), "Please enter the second line of the signboard with one-byte alphanumeric underscore.");
        return;
      }

      Integer capslueToyId = capsluetoy.getDatabase().getCapslueToy(capslueToyName, capslueToyDisplayName, capslueToyDetail, worldName, x, y, z);
      if(capslueToyId == null) {
        event.setCancelled(true);
        throw new Exception("Can not get CapslueToy. CapslueToyName=" + capslueToyName);
      }

      event.setLine(0, ChatColor.translateAlternateColorCodes('&', capsluetoy.getConfig().getString("sign-line1-prefix") + capslueToyDisplayName));
      event.setLine(1, ChatColor.translateAlternateColorCodes('&', capsluetoy.getConfig().getString("sign-line2-prefix") + capslueToyDetail));
      event.setLine(2, ChatColor.translateAlternateColorCodes('&', capsluetoy.getConfig().getString("sign-line3")));
      event.setLine(3, ChatColor.translateAlternateColorCodes('&', capsluetoy.getConfig().getString("sign-line4")));

    } catch (Exception e){
      CapslueToyUtility.logStackTrace(e);
    }
  }

  /**
   * On player interact
   * @param PlayerInteractEvent event
   */
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
      return;
    }
    Block clickedBlock = event.getClickedBlock();
    BlockData data = clickedBlock.getBlockData();
    if (data instanceof Sign || data instanceof WallSign) {
      signProc(event);
    }else if(data.getMaterial().equals(Material.CHEST)) {
      chestProc(event);
    }
  }

  /**
   * Sign process.
   * @param PlayerInteractEvent event
   */
  private void signProc(PlayerInteractEvent event) {
    try {
      Sign sign = (Sign) event.getClickedBlock().getState();
      Player p = event.getPlayer();

      Location signLoc = sign.getLocation();
      if(!capsluetoy.getDatabase().isCapslueToy(signLoc)) {
        return;
      }
      event.setCancelled(true);

      ItemStack ticket = p.getInventory().getItemInMainHand();
      if( !ticket.getType().equals(Material.PAPER) ) {
        CapslueToyUtility.sendMessage(p, ChatColor.translateAlternateColorCodes('&', capsluetoy.getConfig().getString("hold-the-ticket")));
        return;
      }

      List<String> lores = ticket.getItemMeta().getLore();
      if( lores.size() != 3) {
        CapslueToyUtility.sendMessage(p, ChatColor.translateAlternateColorCodes('&', capsluetoy.getConfig().getString("hold-the-ticket")));
        return;
      }

      String ticketCode = CapslueToyUtility.scanf(CapslueToyCommand.FORMAT_TICKET_CODE, lores.get(2));
      if(!capsluetoy.getDatabase().existsTicket(ticketCode)) {
        CapslueToyUtility.sendMessage(p, ChatColor.translateAlternateColorCodes('&', capsluetoy.getConfig().getString("not-found-ticket-code")));
        return;
      }

      Chest chest = capsluetoy.getDatabase().getCapslueToyChest(signLoc);
      if(chest == null) {
        CapslueToyUtility.sendMessage(p, ChatColor.translateAlternateColorCodes('&', capsluetoy.getConfig().getString("not-found-chest1")));
        CapslueToyUtility.sendMessage(p, ChatColor.translateAlternateColorCodes('&', capsluetoy.getConfig().getString("not-found-chest2")));
        return;
      }

      capsluetoy.getDatabase().deleteTicket(ticketCode);
      p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);

      Inventory iv = chest.getInventory();
      int pick = new Random().nextInt(iv.getSize());
      ItemStack pickItem = iv.getItem(pick);
      if(pickItem == null) {
        CapslueToyUtility.sendMessage(p, ChatColor.translateAlternateColorCodes('&', capsluetoy.getConfig().getString("not-found-pick")));
        return;
      }

      ItemStack sendItem = pickItem.clone();
      p.getInventory().addItem(sendItem);
      CapslueToyUtility.sendMessage(p, ChatColor.translateAlternateColorCodes('&', capsluetoy.getConfig().getString("found-pick")));

    } catch (Exception e){
      CapslueToyUtility.logStackTrace(e);
    }
  }

  /**
   * Chest process.
   * @param PlayerInteractEvent event
   */
  private void chestProc(PlayerInteractEvent event) {
    try {
      Player p = event.getPlayer();
      if(!p.getType().equals(EntityType.PLAYER)){
        return;
      }

      if(!event.getClickedBlock().getType().equals(Material.CHEST)) {
        return;
      }

      if(!CapslueToyUtility.isInPunch(p)) {
        return;
      } else {
        event.setCancelled(true);
      }

      String capslueToyName = CapslueToyUtility.getcapslueToyNameInPunch(p);
      CapslueToyUtility.removePunch(p, capsluetoy);
      if(capslueToyName == null) {
        return;
      }

      Location loc = event.getClickedBlock().getLocation();
      if(capsluetoy.getDatabase().updateCapslueToyChest(capslueToyName, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
        CapslueToyUtility.sendMessage(p, "Updated. capsluetoy_name=" + capslueToyName);
        return;
      }

    } catch (Exception e){
      CapslueToyUtility.logStackTrace(e);
    }
  }
}
