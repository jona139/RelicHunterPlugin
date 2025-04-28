package com.example;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class LaunchPlugin
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(net.runelite.client.plugins.relichunterpoc.RelicHunterPlugin.class);
		RuneLite.main(args);
	}
}