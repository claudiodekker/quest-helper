/*
 * Copyright (c) 2024, Zoinkwiz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.questhelper.helpers.quests.troubledtortugans;

import com.questhelper.collections.ItemCollections;
import com.questhelper.panel.PanelDetails;
import com.questhelper.questhelpers.BasicQuestHelper;
import com.questhelper.questinfo.QuestHelperQuest;
import com.questhelper.requirements.Requirement;
import com.questhelper.requirements.conditional.Conditions;
import com.questhelper.requirements.item.ItemRequirement;
import com.questhelper.requirements.player.SkillRequirement;
import com.questhelper.requirements.quest.QuestRequirement;
import com.questhelper.requirements.var.VarbitRequirement;
import com.questhelper.requirements.zone.Zone;
import com.questhelper.requirements.zone.ZoneRequirement;
import com.questhelper.rewards.ExperienceReward;
import com.questhelper.rewards.ItemReward;
import com.questhelper.rewards.QuestPointReward;
import com.questhelper.rewards.UnlockReward;
import com.questhelper.steps.*;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.VarbitID;

import java.util.*;

public class TroubledTortugans extends BasicQuestHelper
{
	// Items Required
	ItemRequirement seaweed, palmLeaf, tortuganBandage, hammer, saw, combatGear, food;

	// Quest items
	ItemRequirement tortuganScute, tortuganScutes6, jatobaLog, jatobaLogs10, seaShell, seaShells6, tortuganShield;

	// Items Recommended
	ItemRequirement teleportItems, prayerPotions, stamina;

	// Quest requirements
	Requirement inStartIsland, inGreatConch, inGryphonCave, inLittlePearl;
	Requirement hasStartedQuest, hasBandage, hasMaterials, hasRepairedAll, hasShield;
	Requirement repaired1, repaired2, repaired3, repaired4, repaired5, repaired6;

	// Quest steps
	DetailedQuestStep talkToInjuredTortugan, getPalmLeaf, getSeaweed, makeBandage, giveBandage;
	DetailedQuestStep talkToFloopa, sailToGreatConch, talkToElderRaley, gatherScutes, gatherLogs, gatherShells;
	DetailedQuestStep repairHouse1, repairHouse2, repairHouse3, repairHouse4, repairHouse5, repairHouse6;
	DetailedQuestStep talkToElderAfterRepairs, talkToElderForHunt, inspectMonument, followTrailPlant1, followTrailRockslide, followTrailPlant2, followTrailPlant3;
	DetailedQuestStep unblockCave, enterGryphonCave, fightGryphon;
	DetailedQuestStep returnToElder, getShield, sailToLittlePearl, fightShellbane, finishQuest;

	// Zones
	Zone startIsland, greatConch, gryphonCave, littlePearl;

	@Override
	public Map<Integer, QuestStep> loadSteps()
	{
		initializeRequirements();
		setupConditions();
		setupSteps();
		Map<Integer, QuestStep> steps = new HashMap<>();

		// Quest start = 0
		steps.put(0, talkToInjuredTortugan);

		// Accepted quest, need to make bandages = 2-4
		ConditionalStep makeBandages = new ConditionalStep(this, getSeaweed);
		makeBandages.addStep(new Conditions(seaweed, palmLeaf), makeBandage);
		makeBandages.addStep(seaweed, getPalmLeaf);
		steps.put(2, makeBandages);
		steps.put(4, makeBandages);

		// Give bandages to Floopa = 6
		steps.put(6, giveBandage);

		// Talk to Floopa and sail = 7-8
		ConditionalStep sailToConch = new ConditionalStep(this, talkToFloopa);
		sailToConch.addStep(inStartIsland, sailToGreatConch);
		steps.put(7, sailToConch);
		steps.put(8, sailToConch);

		// Talk to Elder Raley = 10
		steps.put(10, talkToElderRaley);

		// Gather materials and repair houses = 12-18
		ConditionalStep repairTown = new ConditionalStep(this, gatherMaterials());
		repairTown.addStep(new Conditions(hasMaterials, repaired1, repaired2, repaired3, repaired4, repaired5), repairHouse6);
		repairTown.addStep(new Conditions(hasMaterials, repaired1, repaired2, repaired3, repaired4), repairHouse5);
		repairTown.addStep(new Conditions(hasMaterials, repaired1, repaired2, repaired3), repairHouse4);
		repairTown.addStep(new Conditions(hasMaterials, repaired1, repaired2), repairHouse3);
		repairTown.addStep(new Conditions(hasMaterials, repaired1), repairHouse2);
		repairTown.addStep(hasMaterials, repairHouse1);

		steps.put(12, repairTown);
		steps.put(13, repairTown);
		steps.put(14, repairTown);
		steps.put(16, repairTown);
		steps.put(18, repairTown);

		// Talk to Elder after repairs = 20
		steps.put(20, talkToElderAfterRepairs);

		// Start gryphon hunt = 22
		steps.put(22, talkToElderForHunt);

		// Track gryphon = 24-26
		ConditionalStep trackGryphon = new ConditionalStep(this, inspectMonument);
		trackGryphon.addStep(new VarbitRequirement(VarbitID.TT_HUNTING_TRAIL_5, 1), unblockCave);
		trackGryphon.addStep(new VarbitRequirement(VarbitID.TT_HUNTING_TRAIL_4, 1), followTrailPlant3);
		trackGryphon.addStep(new VarbitRequirement(VarbitID.TT_HUNTING_TRAIL_3, 1), followTrailPlant2);
		trackGryphon.addStep(new VarbitRequirement(VarbitID.TT_HUNTING_TRAIL_2, 1), followTrailRockslide);
		trackGryphon.addStep(new VarbitRequirement(VarbitID.TT_HUNTING_TRAIL_1, 1), followTrailPlant1);
		steps.put(24, trackGryphon);
		steps.put(26, trackGryphon);

		// Unblock cave = 28
		steps.put(28, unblockCave);

		// Enter cave and fight gryphon = 30
		ConditionalStep huntGryphon = new ConditionalStep(this, enterGryphonCave);
		huntGryphon.addStep(inGryphonCave, fightGryphon);
		steps.put(30, huntGryphon);

		// Fight gryphon (if still in progress) = 32
		steps.put(32, fightGryphon);

		// Get shield and sail to Little Pearl = 34
		ConditionalStep prepareForFinal = new ConditionalStep(this, returnToElder);
		prepareForFinal.addStep(hasShield, sailToLittlePearl);
		prepareForFinal.addStep(inGreatConch, getShield);
		steps.put(34, prepareForFinal);

		// Return to Elder after gryphon fight = 36
		steps.put(36, returnToElder);

		// Get Tortugan shield = 38
		steps.put(38, getShield);

		// Sail to Little Pearl = 40
		ConditionalStep goToLittlePearl = new ConditionalStep(this, sailToLittlePearl);
		goToLittlePearl.addStep(inLittlePearl, fightShellbane);
		steps.put(40, goToLittlePearl);

		// Fight Shellbane = 42
		steps.put(42, fightShellbane);

		// Complete quest = 44
		steps.put(44, finishQuest);

		return steps;
	}

	@Override
	protected void setupRequirements()
	{
		// Basic items
		seaweed = new ItemRequirement("Seaweed", 401);
		palmLeaf = new ItemRequirement("Palm leaf", 2339);
		tortuganBandage = new ItemRequirement("Tortugan bandage", 31392);
		hammer = new ItemRequirement("Hammer", ItemCollections.HAMMER).isNotConsumed();
		saw = new ItemRequirement("Saw", ItemCollections.SAW).isNotConsumed();

		// Quest materials
		tortuganScute = new ItemRequirement("Tortugan scute", 31393);
		tortuganScutes6 = new ItemRequirement("Tortugan scutes", 31393, 6);
		jatobaLog = new ItemRequirement("Jatoba log", 32902);
		jatobaLogs10 = new ItemRequirement("Jatoba logs", 32902, 10);
		seaShell = new ItemRequirement("Sea shell", 31395);
		seaShells6 = new ItemRequirement("Sea shells", 31395, 6);
		tortuganShield = new ItemRequirement("Tortugan shield", 31398);

		// Combat gear
		combatGear = new ItemRequirement("Combat gear for Gryphon (level 95) and Shellbane (level 235)", -1, -1).isNotConsumed();
		combatGear.setDisplayItemId(ItemID.ABYSSAL_WHIP);
		food = new ItemRequirement("Food", ItemCollections.GOOD_EATING_FOOD);
		prayerPotions = new ItemRequirement("Prayer potions", ItemCollections.PRAYER_POTIONS);

		// Recommended
		teleportItems = new ItemRequirement("Teleport items (for banking)", -1, -1);
		stamina = new ItemRequirement("Stamina potions", ItemCollections.STAMINA_POTIONS);
	}

	protected void setupConditions()
	{
		// Zones
		startIsland = new Zone(new WorldPoint(2960, 2600, 0), new WorldPoint(2970, 2610, 0));
		inStartIsland = new ZoneRequirement(startIsland);

		// The Great Conch main island (expanded to include all quest areas)
		greatConch = new Zone(new WorldPoint(3100, 2330, 0), new WorldPoint(3240, 2480, 0));
		inGreatConch = new ZoneRequirement(greatConch);

		// Gryphon cave (instanced area)
		gryphonCave = new Zone(new WorldPoint(10710, 8160, 0), new WorldPoint(10750, 8180, 0));
		inGryphonCave = new ZoneRequirement(gryphonCave);

		// Little Pearl island
		littlePearl = new Zone(new WorldPoint(12650, 9520, 0), new WorldPoint(12700, 9560, 0));
		inLittlePearl = new ZoneRequirement(littlePearl);

		// Quest progress conditions
		hasStartedQuest = new VarbitRequirement(VarbitID.TT, 2);
		hasBandage = new VarbitRequirement(VarbitID.TT, 6);

		// Repair tracking
		repaired1 = new VarbitRequirement(VarbitID.TT_REPAIR_KRILL_STALL, 2); // Fish stall (58420)
		repaired2 = new VarbitRequirement(VarbitID.TT_REPAIR_COCO_STALL, 2); // Crafting stall (58421)
		repaired3 = new VarbitRequirement(VarbitID.TT_REPAIR_STROM_CRATES, 2); // Broken crate (58422)
		repaired4 = new VarbitRequirement(VarbitID.TT_REPAIR_STROM_WALL, 2); // Damaged wall (58425)
		repaired5 = new VarbitRequirement(VarbitID.TT_REPAIR_COCO_CRATES, 2); // Coconut crate (58423)
		repaired6 = new VarbitRequirement(VarbitID.TT_REPAIR_KRILL_WALL, 2); // Damaged wall (58424)

		hasMaterials = new Conditions(tortuganScutes6, jatobaLogs10, seaShells6);
		hasRepairedAll = new Conditions(repaired1, repaired2, repaired3, repaired4, repaired5, repaired6);
		hasShield = new VarbitRequirement(VarbitID.TT_FREE_SHIELD, 2);
	}

	protected void setupSteps()
	{
		// Quest start
		talkToInjuredTortugan = new NpcStep(this, NpcID.TT_FLOOPA_INJURED_VIS, new WorldPoint(2962, 2605, 0), "Talk to the Injured Tortugan on the small island.");
		talkToInjuredTortugan.addDialogStep("Yes.");

		// Make bandages
		getPalmLeaf = new ObjectStep(this, 58415, new WorldPoint(2965, 2607, 0), "Shake a palm tree to get a palm leaf.");
		getSeaweed = new DetailedQuestStep(this, "Pick up seaweed from near the shore or bring your own.", seaweed);
		makeBandage = new DetailedQuestStep(this, "Use the seaweed on the palm leaf to make bandages.", seaweed, palmLeaf);
		giveBandage = new NpcStep(this, NpcID.TT_FLOOPA_INJURED_VIS, new WorldPoint(2962, 2605, 0), "Give the bandages to Floopa.", tortuganBandage);

		// Sail to Great Conch
		talkToFloopa = new NpcStep(this, NpcID.TT_FLOOPA_ISLAND, new WorldPoint(2962, 2605, 0), "Talk to Floopa.");
		sailToGreatConch = new DetailedQuestStep(this, "Sail to The Great Conch with Floopa.");

		// Talk to Elder
		talkToElderRaley = new NpcStep(this, NpcID.TT_RALEY_CONCH, new WorldPoint(3186, 2405, 0), "Talk to Elder Raley in The Great Conch.");

		// Gather materials
		gatherScutes = new DetailedQuestStep(this, "Gather 6 Tortugan scutes around The Great Conch.", tortuganScutes6);
		gatherLogs = new ObjectStep(this, 58555, new WorldPoint(3111, 2412, 0), "Chop 10 Jatoba trees for logs.", jatobaLogs10);
		gatherShells = new DetailedQuestStep(this, "Gather 6 sea shells around The Great Conch.", seaShells6);

		// Repair houses
		repairHouse1 = new ObjectStep(this, 58420, new WorldPoint(3167, 2415, 0), "Repair the broken fish stall.", hammer, saw, tortuganScute, jatobaLog, seaShell);
		repairHouse2 = new ObjectStep(this, 58421, new WorldPoint(3172, 2404, 0), "Repair the broken crafting stall.", hammer, saw, tortuganScute, jatobaLog, seaShell);
		repairHouse3 = new ObjectStep(this, 58422, new WorldPoint(3156, 2403, 0), "Repair the broken crate.", hammer, saw, tortuganScute, jatobaLog, seaShell);
		repairHouse4 = new ObjectStep(this, 58425, new WorldPoint(3151, 2412, 0), "Repair the first damaged wall.", hammer, saw, tortuganScute, jatobaLog, seaShell);
		repairHouse5 = new ObjectStep(this, 58423, new WorldPoint(3168, 2402, 0), "Repair the broken coconut crate.", hammer, saw, tortuganScute, jatobaLog, seaShell);
		repairHouse6 = new ObjectStep(this, 58424, new WorldPoint(3167, 2420, 0), "Repair the second damaged wall.", hammer, saw, tortuganScute, jatobaLog, seaShell);

		// Talk to Elder after repairs
		talkToElderAfterRepairs = new NpcStep(this, NpcID.TT_RALEY_CONCH, new WorldPoint(3186, 2405, 0), "Talk to Elder Raley after completing all repairs.");

		// Gryphon hunt - tracking
		talkToElderForHunt = new NpcStep(this, NpcID.TT_RALEY_CONCH, new WorldPoint(3186, 2405, 0), "Talk to Elder Raley to start hunting the Gryphon.");
		inspectMonument = new ObjectStep(this, 58445, new WorldPoint(3167, 2411, 0), "Inspect the Monument in the middle of town to pick up the Gryphon's trail.");
		followTrailPlant1 = new ObjectStep(this, 58455, new WorldPoint(3128, 2423, 0), "Follow the trail by inspecting the Plant northwest of town.");
		followTrailRockslide = new ObjectStep(this, 58467, new WorldPoint(3127, 2448, 0), "Continue following the trail by inspecting the Rockslide.");
		followTrailPlant2 = new ObjectStep(this, 58461, new WorldPoint(3138, 2469, 0), "Continue following the trail by inspecting the Plant.");
		followTrailPlant3 = new ObjectStep(this, 58461, new WorldPoint(3153, 2475, 0), "Follow the trail to the final Plant near the cave entrance.");
		unblockCave = new ObjectStep(this, 58439, new WorldPoint(3177, 2477, 0), "Unblock the cave entrance to access the Gryphon's lair.", combatGear, food);

		// Gryphon fight
		enterGryphonCave = new DetailedQuestStep(this, new WorldPoint(3177, 2477, 0), "Enter the Gryphon's cave.", combatGear, food);
		fightGryphon = new NpcStep(this, new int[]{34900, 55079}, "Defeat the Gryphon (level 95).", combatGear, food);

		// Final battle
		returnToElder = new NpcStep(this, NpcID.TT_RALEY_CONCH, new WorldPoint(3186, 2405, 0), "Return to Elder Raley.");
		getShield = new NpcStep(this, NpcID.TORTUGAN_BLUNN_1OP, new WorldPoint(3172, 2417, 0), "Get the Tortugan shield from Elder Blunn.", tortuganShield);
		sailToLittlePearl = new DetailedQuestStep(this, "Sail to Little Pearl island to confront the Shellbane gryphon.", combatGear, food, prayerPotions, tortuganShield);
		fightShellbane = new NpcStep(this, new int[]{49956, 62304}, "Defeat the Shellbane gryphon (level 235). Use the Tortugan shield to protect against its attacks.", combatGear, food, prayerPotions, tortuganShield);

		finishQuest = new NpcStep(this, NpcID.TT_RALEY_CONCH, new WorldPoint(3186, 2405, 0), "Return to Elder Raley to complete the quest.");
	}

	private QuestStep gatherMaterials()
	{
		ConditionalStep gatherAll = new ConditionalStep(this, gatherScutes);
		gatherAll.addStep(new Conditions(tortuganScutes6, jatobaLogs10), gatherShells);
		gatherAll.addStep(tortuganScutes6, gatherLogs);
		return gatherAll;
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		return Arrays.asList(hammer, saw);
	}

	@Override
	public List<ItemRequirement> getItemRecommended()
	{
		return Arrays.asList(combatGear, food, prayerPotions, teleportItems, stamina);
	}

	@Override
	public List<String> getCombatRequirements()
	{
		return Arrays.asList(
			"Gryphon (level 95)",
			"Shellbane gryphon (level 235)"
		);
	}

	@Override
	public List<Requirement> getGeneralRequirements()
	{
		List<Requirement> reqs = new ArrayList<>();
		reqs.add(new SkillRequirement(Skill.SLAYER, 51, true));
		reqs.add(new SkillRequirement(Skill.CONSTRUCTION, 48, true));
		reqs.add(new SkillRequirement(Skill.SAILING, 45, true));
		reqs.add(new SkillRequirement(Skill.HUNTER, 45, true));
		reqs.add(new SkillRequirement(Skill.WOODCUTTING, 40, true));
		reqs.add(new SkillRequirement(Skill.CRAFTING, 34, true));
//		 reqs.add(new QuestRequirement(QuestHelperQuest.PANDEMONIUM, QuestState.FINISHED));
		return reqs;
	}

	@Override
	public QuestPointReward getQuestPointReward()
	{
		return new QuestPointReward(1);
	}

	@Override
	public List<ExperienceReward> getExperienceRewards()
	{
		return Arrays.asList(
			new ExperienceReward(Skill.SAILING, 10000),
			new ExperienceReward(Skill.SLAYER, 8000)
		);
	}

	@Override
	public List<ItemReward> getItemRewards()
	{
		return Arrays.asList(
			new ItemReward("Tortugan Shield", 31398, 1),
			new ItemReward("Coins", 995, 5000)
		);
	}

	@Override
	public List<UnlockReward> getUnlockRewards()
	{
		return Arrays.asList(
			new UnlockReward("Access to The Great Conch"),
			new UnlockReward("Ability to craft Tortugan equipment")
		);
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();

		allSteps.add(new PanelDetails("Starting Out", Arrays.asList(talkToInjuredTortugan, getPalmLeaf, getSeaweed, makeBandage, giveBandage)));
		allSteps.add(new PanelDetails("Travel to The Great Conch", Arrays.asList(talkToFloopa, sailToGreatConch, talkToElderRaley)));
		allSteps.add(new PanelDetails("Repair the Town", Arrays.asList(gatherScutes, gatherLogs, gatherShells, repairHouse1, repairHouse2, repairHouse3, repairHouse4, repairHouse5, repairHouse6, talkToElderAfterRepairs), hammer, saw));
		allSteps.add(new PanelDetails("Hunt the Gryphon", Arrays.asList(talkToElderForHunt, inspectMonument, followTrailPlant1, followTrailRockslide, followTrailPlant2, followTrailPlant3, unblockCave, enterGryphonCave, fightGryphon), combatGear, food));
		allSteps.add(new PanelDetails("Final Battle", Arrays.asList(returnToElder, getShield, sailToLittlePearl, fightShellbane, finishQuest), combatGear, food, prayerPotions));

		return allSteps;
	}
}
