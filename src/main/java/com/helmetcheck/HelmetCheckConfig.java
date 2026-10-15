package com.helmetcheck;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("helmetcheck")
public interface HelmetCheckConfig extends Config
{
	@ConfigItem(
			keyName = "emptyMessage",
			name = "Empty slot message",
			description = "The chat message shown when nothing is equipped in your head slot",
			position = 1
	)
	default String emptyMessage()
	{
		return "Helmet Check: your head slot is empty!";
	}

	@ConfigItem(
			keyName = "announceEquipped",
			name = "Announce equipped helmet",
			description = "Also show a chat message when you equip a helmet",
			position = 2
	)
	default boolean announceEquipped()
	{
		return true;
	}

	@ConfigItem(
			keyName = "equippedMessage",
			name = "Equipped message",
			description = "The message shown when a helmet is equipped. Use {item} for the item name",
			position = 3
	)
	default String equippedMessage()
	{
		return "Helmet Check: you are wearing {item}.";
	}

	@ConfigItem(
			keyName = "exceptions",
			name = "Ignored helmets",
			description = "Comma-separated item names that will not trigger a message. Case-insensitive, and * works as a wildcard (e.g. Slayer helmet*, Black mask)",
			position = 4
	)
	default String exceptions()
	{
		return "";
	}
}