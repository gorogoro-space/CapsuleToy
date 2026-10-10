package space.gorogoro.capsuletoy;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * CapsuleToy
 * @license    GPLv3
 * @copyright  Copyright gorogoro.space 2021
 * @author     kubotan
 * @see        <a href="https://gorogoro.space">gorogoro.space</a>
 */
public class CapsuleToy extends JavaPlugin{
  private CapsuleToyDatabase database;
  private CapsuleToyCommand command;
  private CapsuleToyListener listener;
  private final Set<String> namedTicketOnly = new HashSet<String>();

  /**
   * Get CapsuleToyDatabase instance.
   */
  public CapsuleToyDatabase getDatabase() {
    return database;
  }

  // 番号だけの券。named-ticket-only に無い看板で使える。所持品へは入れない
  public ItemStack createCodedTicket() {
    return CapsuleToyUtility.createCodedTicket(this, null);
  }

  // 指定した名前の看板でしか使えない券。所持品へは入れない
  public ItemStack createCodedTicket(String capsuleName) {
    return CapsuleToyUtility.createCodedTicket(this, capsuleName);
  }

  /**
   * Get CapsuleToyCommand instance.
   */
  public CapsuleToyCommand getCommand() {
    return command;
  }

  /**
   * Get CapsuleToyListener instance.
   */
  public CapsuleToyListener getListener() {
    return listener;
  }

  // named-ticket-only に書いた看板は、名前の無い券を受け付けない。大文字と小文字は区別する
  public boolean requiresNamedTicket(String signName) {
    return signName != null && namedTicketOnly.contains(signName);
  }

  // 起動時と /capsuletoy reload だけで読む
  public void loadSettings() {
    namedTicketOnly.clear();
    Object raw = getConfig().get("named-ticket-only");
    if (raw == null) {
      namedTicketOnly.add("infernal");
    } else if (raw instanceof String) {
      addNamedTicketOnly((String) raw);
    } else if (raw instanceof List) {
      for (Object item : (List<?>) raw) {
        if (item != null) {
          addNamedTicketOnly(String.valueOf(item));
        }
      }
    } else {
      getLogger().warning("named-ticket-only の形が違うので、infernal だけにします。");
      namedTicketOnly.add("infernal");
    }
    if (raw != null && namedTicketOnly.isEmpty() && !(raw instanceof List && ((List<?>) raw).isEmpty())) {
      getLogger().warning("named-ticket-only に使える名前が無いので、名前の無い券はどの看板でも使えます。");
    }
    CapsuleToyUtility.loadMessages(this);
  }

  private void addNamedTicketOnly(String name) {
    String trimmed = name.trim();
    if (trimmed.isEmpty()) {
      return;
    }
    if (!trimmed.matches("[0-9A-Za-z_]+")) {
      getLogger().warning("named-ticket-only: 使えない名前を読み飛ばしました: " + trimmed);
      return;
    }
    namedTicketOnly.add(trimmed);
  }

  /**
   * JavaPlugin method onEnable.
   */
  @Override
  public void onEnable(){
    try{
      getLogger().log(Level.INFO, "The Plugin Has Been Enabled!");

      // If there is no setting file, it is created
      if(!getDataFolder().exists()){
        getDataFolder().mkdir();
      }

      File configFile = new File(getDataFolder(), "config.yml");
      if(!configFile.exists()){
        saveDefaultConfig();
      }

      // copy languge template
      ArrayList<String> langFileNameList = new ArrayList<String>(
        Arrays.asList(
          "config_jp.yml"
          // ,"config_fr.yml"   // add here language
        )
      );
      for (String curFileName : langFileNameList) {
        InputStream in = getResource(curFileName);
        Files.copy(in, new File(getDataFolder(), curFileName).toPath(), StandardCopyOption.REPLACE_EXISTING);
        in.close();
      }

      loadSettings();

      // Initialize the database.
      database = new CapsuleToyDatabase(this);
      database.initialize();

      // Register event listener.
      PluginManager pm = getServer().getPluginManager();
      HandlerList.unregisterAll(this);    // clean up
      listener = new CapsuleToyListener(this);
      pm.registerEvents(listener, this);

      // Instance prepared of CapsuleToyCommand.
      command = new CapsuleToyCommand(this);

    } catch (Exception e){
      CapsuleToyUtility.logStackTrace(e);
    }
  }

  /**
   * JavaPlugin method onCommand.
   *
   * @return boolean true:Success false:Display the usage dialog set in plugin.yml
   */
  public boolean onCommand( CommandSender sender, Command commandInfo, String label, String[] args) {
    boolean hideUseageFlag = true;  // true:Success false:Display the usage dialog set in plugin.yml
    try{
      if(!commandInfo.getName().equals("capsuletoy")) {
        return hideUseageFlag;
      }

      if(args.length <= 0) {
        return hideUseageFlag;
      }
      String subCommand = args[0];

      command.initialize(sender, args);
      switch(subCommand) {
        case "list":
          if(sender.hasPermission("capsuletoy.list")) {
            hideUseageFlag = command.list();
          }
          break;

        case "modify":
          if(sender.hasPermission("capsuletoy.modify")) {
            hideUseageFlag = command.modify();
          }
          break;

        case "delete":
          if(sender.hasPermission("capsuletoy.delete")) {
            hideUseageFlag = command.delete();
          }
          break;

        case "ticket":
          if((sender instanceof BlockCommandSender) || (sender instanceof ConsoleCommandSender) || sender.isOp()) {
            for(Player p:CapsuleToyUtility.getTarget(this, args[1], sender)) {  // @a @p @s @r or playername
              command.ticket(p);
            }
            hideUseageFlag = true;
          }
          break;

        case "enable":
          if(sender.isOp()) {
            hideUseageFlag = command.enable();
          }
          break;

        case "reload":
          if(sender.isOp()) {
            hideUseageFlag = command.reload();
          }
          break;

        case "disable":
          if(sender.isOp()) {
            hideUseageFlag = command.disable();
          }
          break;

        default:
          hideUseageFlag = false;
      }
    }catch(Exception e){
      CapsuleToyUtility.logStackTrace(e);
    }finally{
      command.finalize();
    }
    return hideUseageFlag;
  }

  /**
   * JavaPlugin method onDisable.
   */
  @Override
  public void onDisable(){
    try{
      database.finalize();
      command.finalize();

      // Unregister all event listener.
      HandlerList.unregisterAll(this);

      getLogger().log(Level.INFO, "The Plugin Has Been Disabled!");
    } catch (Exception e){
      CapsuleToyUtility.logStackTrace(e);
    }
  }
}
