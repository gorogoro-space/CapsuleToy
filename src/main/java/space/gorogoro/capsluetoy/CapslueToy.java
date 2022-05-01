package space.gorogoro.capsluetoy;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * CapslueToy
 * @license    GPLv3
 * @copyright  Copyright gorogoro.space 2021
 * @author     kubotan
 * @see        <a href="https://gorogoro.space">gorogoro.space</a>
 */
public class CapslueToy extends JavaPlugin{
  private CapslueToyDatabase database;
  private CapslueToyCommand command;
  private CapslueToyListener listener;

  /**
   * Get CapslueToyDatabase instance.
   */
  public CapslueToyDatabase getDatabase() {
    return database;
  }

  /**
   * Get CapslueToyCommand instance.
   */
  public CapslueToyCommand getCommand() {
    return command;
  }

  /**
   * Get CapslueToyListener instance.
   */
  public CapslueToyListener getListener() {
    return listener;
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

      // Initialize the database.
      database = new CapslueToyDatabase(this);
      database.initialize();

      // Register event listener.
      PluginManager pm = getServer().getPluginManager();
      HandlerList.unregisterAll(this);    // clean up
      listener = new CapslueToyListener(this);
      pm.registerEvents(listener, this);

      // Instance prepared of CapslueToyCommand.
      command = new CapslueToyCommand(this);

    } catch (Exception e){
      CapslueToyUtility.logStackTrace(e);
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
      if(!commandInfo.getName().equals("capsluetoy")) {
        return hideUseageFlag;
      }

      if(args.length <= 0) {
        return hideUseageFlag;
      }
      String subCommand = args[0];

      command.initialize(sender, args);
      switch(subCommand) {
        case "list":
          if(sender.hasPermission("capsluetoy.list")) {
            hideUseageFlag = command.list();
          }
          break;

        case "modify":
          if(sender.hasPermission("capsluetoy.modify")) {
            hideUseageFlag = command.modify();
          }
          break;

        case "delete":
          if(sender.hasPermission("capsluetoy.delete")) {
            hideUseageFlag = command.delete();
          }
          break;

        case "ticket":
          if((sender instanceof BlockCommandSender) || (sender instanceof ConsoleCommandSender) || sender.isOp()) {
            for(Player p:CapslueToyUtility.getTarget(this, args[1], sender)) {  // @a @p @s @r or playername
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
      CapslueToyUtility.logStackTrace(e);
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
      CapslueToyUtility.logStackTrace(e);
    }
  }
}
