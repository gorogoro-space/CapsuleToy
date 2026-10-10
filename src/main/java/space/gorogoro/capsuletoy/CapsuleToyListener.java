package space.gorogoro.capsuletoy;

import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
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
 * CapsuleToyListener
 * @license    GPLv3
 * @copyright  Copyright gorogoro.space 2021
 * @author     kubotan
 * @see        <a href="https://gorogoro.space">gorogoro.space</a>
 */
public class CapsuleToyListener implements Listener{
  private CapsuleToy capsuletoy;

  /**
   * Constructor of CapsuleToyListener.
   */
  public CapsuleToyListener(CapsuleToy capsuletoy) {
    try{
      this.capsuletoy = capsuletoy;
    } catch (Exception e){
      CapsuleToyUtility.logStackTrace(e);
    }
  }

  /**
   * On sign change
   * @param SignChangeEvent event
   */
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onSignChange(SignChangeEvent event) {
    try {
      if(!CapsuleToyUtility.fromSignComponent(event.line(0)).toLowerCase().equals("[capsuletoy]")) {
        return;
      }

      if(!event.getPlayer().hasPermission("capsuletoy.create")) {
        return;
      }

      Location signLoc = event.getBlock().getLocation();
      if(capsuletoy.getDatabase().isCapsuleToy(signLoc)) {
        event.setCancelled(true);
        CapsuleToyUtility.sendMessage(event.getPlayer(), "It is already registered. To continue, please delete first.");
        return;
      }

      String capsuleToyName = CapsuleToyUtility.fromSignComponent(event.line(1));
      String capsuleToyDisplayName = CapsuleToyUtility.fromSignComponent(event.line(2));
      String capsuleToyDetail = CapsuleToyUtility.fromSignComponent(event.line(3));
      String worldName = signLoc.getWorld().getName();
      Integer x = signLoc.getBlockX();
      Integer y = signLoc.getBlockY();
      Integer z = signLoc.getBlockZ();
      Pattern p = Pattern.compile("^[0-9a-zA-Z_]+$");
      if(!p.matcher(capsuleToyName).find()) {
        event.setCancelled(true);
        CapsuleToyUtility.sendMessage(event.getPlayer(), "Please enter the second line of the signboard with one-byte alphanumeric underscore.");
        return;
      }

      Integer capsuleToyId = capsuletoy.getDatabase().getCapsuleToy(capsuleToyName, capsuleToyDisplayName, capsuleToyDetail, worldName, x, y, z);
      if(capsuleToyId == null) {
        event.setCancelled(true);
        throw new Exception("Can not get CapsuleToy. CapsuleToyName=" + capsuleToyName);
      }

      event.line(0, CapsuleToyUtility.toSignComponent(CapsuleToyUtility.translateAlternateColorCodes('&', capsuletoy.getConfig().getString("sign-line1-prefix") + capsuleToyDisplayName)));
      event.line(1, CapsuleToyUtility.toSignComponent(CapsuleToyUtility.translateAlternateColorCodes('&', capsuletoy.getConfig().getString("sign-line2-prefix") + capsuleToyDetail)));
      event.line(2, CapsuleToyUtility.toSignComponent(CapsuleToyUtility.translateAlternateColorCodes('&', capsuletoy.getConfig().getString("sign-line3"))));
      event.line(3, CapsuleToyUtility.toSignComponent(CapsuleToyUtility.translateAlternateColorCodes('&', capsuletoy.getConfig().getString("sign-line4"))));

    } catch (Exception e){
      CapsuleToyUtility.logStackTrace(e);
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
      if(!capsuletoy.getDatabase().isCapsuleToy(signLoc)) {
        return;
      }
      event.setCancelled(true);

      ItemStack ticket = p.getInventory().getItemInMainHand();

      if( !ticket.getType().equals(Material.PAPER) ) {
        CapsuleToyUtility.sendMessage(p, CapsuleToyUtility.translateAlternateColorCodes('&', capsuletoy.getConfig().getString("hold-the-ticket")));
        return;
      }

      List<Component> lores = ticket.getItemMeta().lore();
      if( lores == null || lores.size() < 3) {
        CapsuleToyUtility.sendMessage(p, CapsuleToyUtility.translateAlternateColorCodes('&', capsuletoy.getConfig().getString("hold-the-ticket")));
        return;
      }

      // 色コードを除いた文字だけで読む。これまでに配った券(String 版 API で作ったもの)も同じ番号になる
      String ticketCode = CapsuleToyUtility.toPlainText(lores.get(2)).replace(CapsuleToyCommand.PREFIX_TICKET_CODE, "");
      ticketCode = ticketCode.replace("CAPSLUETOY CODE:", "");
      String boundName = capsuletoy.getDatabase().findTicketCapsule(ticketCode);
      if(boundName == null) {
        CapsuleToyUtility.sendMessage(p, CapsuleToyUtility.translateAlternateColorCodes('&', capsuletoy.getConfig().getString("not-found-ticket-code")));
        return;
      }
      String signName = capsuletoy.getDatabase().getCapsuleToyName(signLoc);
      if(!boundName.isEmpty()) {
        if(!boundName.equals(signName)) {
          String message = CapsuleToyUtility.configMessage(capsuletoy, "wrong-capsule");
          message = message.replace("{0}", boundName);
          CapsuleToyUtility.sendMessage(p, CapsuleToyUtility.translateAlternateColorCodes('&', message));
          return;
        }
      } else if(capsuletoy.requiresNamedTicket(signName)) {
        String message = CapsuleToyUtility.configMessage(capsuletoy, "needs-named-ticket");
        message = message.replace("{0}", signName);
        CapsuleToyUtility.sendMessage(p, CapsuleToyUtility.translateAlternateColorCodes('&', message));
        return;
      }

      Chest chest = capsuletoy.getDatabase().getCapsuleToyChest(signLoc);
      if(chest == null) {
        CapsuleToyUtility.sendMessage(p, CapsuleToyUtility.translateAlternateColorCodes('&', capsuletoy.getConfig().getString("not-found-chest1")));
        CapsuleToyUtility.sendMessage(p, CapsuleToyUtility.translateAlternateColorCodes('&', capsuletoy.getConfig().getString("not-found-chest2")));
        return;
      }

      capsuletoy.getDatabase().deleteTicket(ticketCode);
      p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);

      Inventory iv = chest.getInventory();
      int pick = new Random().nextInt(iv.getSize());
      ItemStack pickItem = iv.getItem(pick);
      if(pickItem == null) {
        CapsuleToyUtility.sendMessage(p, CapsuleToyUtility.translateAlternateColorCodes('&', capsuletoy.getConfig().getString("not-found-pick")));
        return;
      }

      givePrize(p, pickItem);

    } catch (Exception e){
      CapsuleToyUtility.logStackTrace(e);
    }
  }

  private void givePrize(Player player, ItemStack prize) {
    boolean stored = CapsuleToyUtility.giveOrDrop(player, prize);
    String key = stored ? "found-pick" : "dropped-pick";
    String message = CapsuleToyUtility.configMessage(capsuletoy, key);
    CapsuleToyUtility.sendMessage(player, CapsuleToyUtility.translateAlternateColorCodes('&', message));
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

      if(!CapsuleToyUtility.isInPunch(p)) {
        return;
      } else {
        event.setCancelled(true);
      }

      String capsuleToyName = CapsuleToyUtility.getcapsuleToyNameInPunch(p);
      CapsuleToyUtility.removePunch(p, capsuletoy);
      if(capsuleToyName == null) {
        return;
      }

      Location loc = event.getClickedBlock().getLocation();
      if(capsuletoy.getDatabase().updateCapsuleToyChest(capsuleToyName, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
        CapsuleToyUtility.sendMessage(p, "Updated. capsuletoy_name=" + capsuleToyName);
        return;
      }

    } catch (Exception e){
      CapsuleToyUtility.logStackTrace(e);
    }
  }
}
