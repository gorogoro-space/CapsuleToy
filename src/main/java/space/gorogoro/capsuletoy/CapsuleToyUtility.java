package space.gorogoro.capsuletoy;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
  private static final char COLOR_CHAR = '§';
  private static final String COLOR_CODE_CHARS = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";
  // アイテムの名前・説明文用。Bukkit の String 版 API と同じ形式(§ と、§x§r§r§g§g§b§b の hex 色)
  private static final LegacyComponentSerializer ITEM_SERIALIZER = LegacyComponentSerializer.builder()
    .character(COLOR_CHAR)
    .hexColors()
    .useUnusualXRepeatedCharacterHexFormat()
    .build();
  // 看板用。Paper の SignChangeEvent.getLine/setLine の内部と同じもの
  private static final LegacyComponentSerializer SIGN_SERIALIZER = LegacyComponentSerializer.legacySection();
  // modify 中のプレイヤーと、チェストを設定するカプセルトイの登録名
  private static final Map<UUID, String> punches = new HashMap<UUID, String>();

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
    return punches.containsKey(player.getUniqueId());
  }

  /**
   * get capsuletoy name in punch.
   * @param Player Player
   * @return String CapsuleToy name
   */
  public static String getcapsuleToyNameInPunch(Player player){
    return punches.get(player.getUniqueId());
  }
  
  /**
   * Set punch processing.
   * @param Player Player
   * @param CapsuleToy capsuletoy
   * @param String CapsuleToy Name
   */
  public static void setPunch(Player player, CapsuleToy capsuletoy, String capsuleToyName){
    removePunch(player, capsuletoy);
    punches.put(player.getUniqueId(), capsuleToyName);
  }

  /**
   * Remove punch processing.
   * @param Player Player
   * @param CapsuleToy capsuletoy
   */
  public static void removePunch(Player player, CapsuleToy capsuletoy){
    punches.remove(player.getUniqueId());
  }

  // プラグインの終了時に、modify 中の状態をすべて消す
  public static void clearPunches() {
    punches.clear();
  }
  
  /**
   * Send message to player
   * @param CommandSender CommandSender
   * @param String message
   */
  public static void sendMessage(CommandSender sender, String message){
    // §4 は DARK_RED、§c は RED
    sender.sendMessage(COLOR_CHAR + "4[CapsuleToy]" + " " + COLOR_CHAR + "c" + message);
  }

  // altColorChar の色コード(&a など)を § に置き換える(ChatColor.translateAlternateColorCodes と同じ動き)
  public static String translateAlternateColorCodes(char altColorChar, String text) {
    char[] chars = text.toCharArray();
    for (int i = 0; i < chars.length - 1; i++) {
      if (chars[i] == altColorChar && COLOR_CODE_CHARS.indexOf(chars[i + 1]) > -1) {
        chars[i] = COLOR_CHAR;
        chars[i + 1] = Character.toLowerCase(chars[i + 1]);
      }
    }
    return new String(chars);
  }

  // § の色コード付き文字列を、アイテムの名前・説明文用の Component にする
  // 非推奨の String 版 API(setDisplayName など)は斜体を明示的に外しているので、見た目をそろえるため同じようにする
  public static Component toItemComponent(String text) {
    return ITEM_SERIALIZER.deserialize(text).decoration(TextDecoration.ITALIC, false);
  }

  // 説明文の 1 行を、色コードを除いた文字だけにする(券の番号の読み取り用)。null は空文字にする
  public static String toPlainText(Component component) {
    return component == null ? "" : PlainTextComponentSerializer.plainText().serialize(component);
  }

  // 看板の行と文字列の変換(Paper の String 版 API と同じ変換)。null は空文字にする
  public static Component toSignComponent(String text) {
    return SIGN_SERIALIZER.deserialize(text);
  }

  public static String fromSignComponent(Component component) {
    return component == null ? "" : SIGN_SERIALIZER.serialize(component);
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

  // データベースに番号を作って、コード付きの券を 1 枚返す。名前がある券は、その看板でしか使えない
  public static ItemStack createCodedTicket(CapsuleToy capsuletoy, String capsuleName) {
    if (capsuleName != null && capsuleName.isEmpty()) {
      capsuleName = null;
    }
    if (capsuleName != null && !capsuleName.matches("[0-9A-Za-z_]+")) {
      return null;
    }
    String ticketCode = capsuletoy.getDatabase().getTicket(capsuleName);
    if (ticketCode == null) {
      return null;
    }
    ItemStack ticket = new ItemStack(Material.PAPER, 1);
    ItemMeta meta = ticket.getItemMeta();
    String name = capsuletoy.getConfig().getString("ticket-display-name");
    if (name == null) {
      name = "";
    }
    meta.displayName(toItemComponent(translateAlternateColorCodes('&', name)));
    List<Component> lore = new ArrayList<Component>();
    String lore1 = capsuletoy.getConfig().getString("ticket-lore1");
    String lore2 = capsuletoy.getConfig().getString("ticket-lore2");
    lore.add(toItemComponent(translateAlternateColorCodes('&', lore1 == null ? "" : lore1)));
    lore.add(toItemComponent(translateAlternateColorCodes('&', lore2 == null ? "" : lore2)));
    lore.add(toItemComponent(String.format(CapsuleToyCommand.PREFIX_TICKET_CODE + "%s", ticketCode)));
    if (capsuleName != null) {
      // §7 は GRAY
      lore.add(toItemComponent(COLOR_CHAR + "7" + capsuleName));
    }
    meta.lore(lore);
    ticket.setItemMeta(meta);
    return ticket;
  }

  // 入る余地が束の個数以上あるときだけインベントリへ入れる。足りなければ束ごと足元へ落とす
  public static boolean giveOrDrop(Player player, ItemStack item) {
    ItemStack clone = item.clone();
    if (hasEnoughSpace(player, clone)) {
      player.getInventory().addItem(clone);
      return true;
    }
    World world = player.getWorld();
    if (world != null) {
      world.dropItemNaturally(player.getLocation(), clone);
    }
    return false;
  }

  public static boolean hasEnoughSpace(Player player, ItemStack item) {
    int maxStack = item.getMaxStackSize();
    int space = 0;
    for (ItemStack invItem : player.getInventory().getStorageContents()) {
      if (invItem == null) {
        space += maxStack;
      } else if (invItem.isSimilar(item)) {
        space += maxStack - invItem.getAmount();
      }
    }
    return space >= item.getAmount();
  }

  private static YamlConfiguration fileConfig;

  // 起動時と /capsuletoy reload だけで config.yml を読む
  public static void loadMessages(CapsuleToy plugin) {
    File file = new File(plugin.getDataFolder(), "config.yml");
    fileConfig = YamlConfiguration.loadConfiguration(file);
  }

  // config.yml に書いてあればその文。無いときは、ほかの文が日本語なら日本語、それ以外は英語
  public static String configMessage(CapsuleToy plugin, String key) {
    if (fileConfig == null) {
      loadMessages(plugin);
    }
    YamlConfiguration file = fileConfig;
    if (file.contains(key)) {
      String value = file.getString(key);
      if (value != null) {
        return value;
      }
    }
    if (usesJapanese(file)) {
      if ("found-pick".equals(key)) {
        return "賞品をインベントリに送りました。";
      }
      if ("dropped-pick".equals(key)) {
        return "インベントリが一杯なので、足元に落としました。";
      }
      if ("wrong-capsule".equals(key)) {
        return "この券は {0} でしか使えません。";
      }
      if ("needs-named-ticket".equals(key)) {
        return "このカプセルトイは、{0} の券だけで使えます。";
      }
    }
    if ("found-pick".equals(key)) {
      return "Sent the prize to the inventory.";
    }
    if ("dropped-pick".equals(key)) {
      return "Your inventory was full, so the prize was dropped at your feet.";
    }
    if ("wrong-capsule".equals(key)) {
      return "This ticket can only be used at {0}.";
    }
    if ("needs-named-ticket".equals(key)) {
      return "This capsule toy accepts only a {0} ticket.";
    }
    return "";
  }

  private static boolean usesJapanese(YamlConfiguration file) {
    String[] samples = new String[] {"hold-the-ticket", "ticket-display-name", "ticket-lore1", "found-pick"};
    for (int i = 0; i < samples.length; i++) {
      String text = file.getString(samples[i]);
      if (text != null && hasJapanese(text)) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasJapanese(String text) {
    for (int i = 0; i < text.length(); i++) {
      Character.UnicodeBlock block = Character.UnicodeBlock.of(text.charAt(i));
      if (block == Character.UnicodeBlock.HIRAGANA
          || block == Character.UnicodeBlock.KATAKANA
          || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
        return true;
      }
    }
    return false;
  }
}
