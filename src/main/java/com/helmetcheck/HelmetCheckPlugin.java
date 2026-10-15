package com.helmetcheck;

import com.google.inject.Provides;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;

@Slf4j
@PluginDescriptor(
		name = "Helmet Check"
)
public class HelmetCheckPlugin extends Plugin
{
	private static final int NO_ITEM = -1;

	@Inject
	private Client client;

	@Inject
	private HelmetCheckConfig config;

	@Inject
	private ChatMessageManager chatMessageManager;

	// Last head item we reported, so we only message when it actually changes
	private int lastHeadItemId = Integer.MIN_VALUE;

	// Set when something happened that warrants a check; handled on the next game tick
	private boolean checkPending;

	@Override
	protected void startUp() throws Exception
	{
		log.debug("Helmet Check started!");
		lastHeadItemId = Integer.MIN_VALUE;
		checkPending = client.getGameState() == GameState.LOGGED_IN;
	}

	@Override
	protected void shutDown() throws Exception
	{
		lastHeadItemId = Integer.MIN_VALUE;
		checkPending = false;
		log.debug("Helmet Check stopped!");
	}

	@Provides
	HelmetCheckConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HelmetCheckConfig.class);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			lastHeadItemId = Integer.MIN_VALUE;
			checkPending = true;
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == InventoryID.WORN)
		{
			checkPending = true;
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (!checkPending)
		{
			return;
		}
		checkPending = false;

		checkHeadSlot(client.getItemContainer(InventoryID.WORN));
	}

	private void checkHeadSlot(ItemContainer equipment)
	{
		if (equipment == null)
		{
			log.debug("Equipment container not available yet");
			return;
		}

		Item head = equipment.getItem(EquipmentInventorySlot.HEAD.getSlotIdx());
		int headId = (head == null) ? NO_ITEM : head.getId();

		if (headId == lastHeadItemId)
		{
			return;
		}
		lastHeadItemId = headId;

		if (headId == NO_ITEM)
		{
			log.debug("Head slot is EMPTY");
			sendChat(config.emptyMessage());
			return;
		}

		String name = client.getItemDefinition(headId).getName();
		log.debug("Head slot: {} (id={})", name, headId);

		if (isIgnored(name))
		{
			log.debug("{} is in the ignore list, not messaging", name);
			return;
		}

		if (config.announceEquipped())
		{
			sendChat(config.equippedMessage().replace("{item}", name));
		}
	}

	private boolean isIgnored(String itemName)
	{
		List<String> patterns = Text.fromCSV(config.exceptions());
		String lowerName = itemName.toLowerCase(Locale.ROOT);

		for (String pattern : patterns)
		{
			if (WildcardMatcher.matches(pattern.toLowerCase(Locale.ROOT), lowerName))
			{
				return true;
			}
		}
		return false;
	}

	private void sendChat(String message)
	{
		if (message == null || message.trim().isEmpty())
		{
			return;
		}

		chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.GAMEMESSAGE)
				.runeLiteFormattedMessage(message)
				.build());
	}
}