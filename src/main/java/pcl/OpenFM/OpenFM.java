package pcl.OpenFM;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import pcl.OpenFM.GUI.OFMGuiHandler;
import pcl.OpenFM.Handler.ClientEvent;
import pcl.OpenFM.Handler.ServerEvent;
import pcl.OpenFM.network.PacketHandler;
import pcl.OpenFM.player.PlayerDispatcher;
import cpw.mods.fml.client.GuiIngameModOptions;
import cpw.mods.fml.client.GuiModList;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid=BuildInfo.modID, name=BuildInfo.modName, version=BuildInfo.versionNumber + "." + BuildInfo.buildNumber, dependencies = "", guiFactory = "pcl.OpenFM.GUI.OFMGuiFactory")
public class OpenFM {
	public static final String MODID = "openfm";
	@Mod.Instance(BuildInfo.modID)
	public static OpenFM instance;
	@SidedProxy(clientSide="pcl.OpenFM.ClientProxy", serverSide="pcl.OpenFM.CommonProxy")
	public static CommonProxy proxy;
	public static List<PlayerDispatcher> playerList = new ArrayList<PlayerDispatcher>();
	public Configuration config;
	public static final Logger logger = LogManager.getFormatterLogger(BuildInfo.modID);
	public static File configFile;
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		PacketHandler.init();

		// Load config
		configFile = new File(event.getModConfigurationDirectory() + "/openfm/openfm.cfg");
		OFMConfiguration.init(configFile);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
	public void onEvent(GuiOpenEvent event) {
		if (event.gui instanceof GuiIngameModOptions) {
			event.gui = new GuiModList(new GuiIngameMenu());
		}
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new OFMGuiHandler());
		MinecraftForge.EVENT_BUS.register(new ClientEvent());
		FMLCommonHandler.instance().bus().register(new ClientEvent());
		MinecraftForge.EVENT_BUS.register(new ServerEvent());
		FMLCommonHandler.instance().bus().register(new ServerEvent());
		FMLCommonHandler.instance().bus().register(instance);
		MinecraftForge.EVENT_BUS.register(instance);
		ContentRegistry.init();
		proxy.initTileEntities();
		proxy.registerRenderers();
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if(eventArgs.modID.equals(BuildInfo.modID)){
			OFMConfiguration.sync();
		}
	}

	public static void killAllStreams() {
		if (playerList != null) {
			for (PlayerDispatcher p : playerList) {
				p.stop();
			}
		}
	}
}


