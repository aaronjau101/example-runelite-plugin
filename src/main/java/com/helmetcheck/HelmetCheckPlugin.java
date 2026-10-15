package com.helmetcheck;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.callback.ClientThread;
import net.runelite.api.ItemContainer;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.ItemID;

@Slf4j
@PluginDescriptor(
	name = "Helmet Check",
	description = "Warns you if you log in without a head item equipped"
)
public class HelmetCheckPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private HelmetCheckConfig config;

	@Inject
	private ClientThread clientThread;

	@Override
	protected void startUp() throws Exception
	{
		log.debug("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.debug("Example stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "God", "Example says " + config.greeting(), null);

			clientThread.invokeLater(() ->
			{
				ItemContainer equipment = client.getItemContainer(
						net.runelite.api.InventoryID.EQUIPMENT
				);

				if (equipment == null)
				{
					return;
				}

				int headSlot = EquipmentInventorySlot.HEAD.getSlotIdx();

				if (equipment.getItem(headSlot).getId() == -1)
				{
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You are not wearing a head item", null);
				} else {
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You are wearing a head item", null);

				}

				int headItemId = equipment.getItem(headSlot).getId();
				String headItemName = equipment.getItem(headSlot).toString();

				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Head item is " + headItemName, null);


//				if (headItemId == ItemID.BRONZE_MED_HELM)
//				{
//					// Placeholder: replace with the actual empty-slot check below.
//				}
			});
		}
	}

	@Provides
	HelmetCheckConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HelmetCheckConfig.class);
	}
}
