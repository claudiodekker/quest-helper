package com.questhelper.helpers.quests.thebloodmoonrises;

import com.questhelper.bank.banktab.BankSlotIcons;
import com.questhelper.collections.ItemCollections;
import com.questhelper.panel.PanelDetails;
import com.questhelper.questhelpers.BasicQuestHelper;
import com.questhelper.questinfo.QuestHelperQuest;
import com.questhelper.requirements.Requirement;
import com.questhelper.requirements.conditional.Conditions;
import com.questhelper.requirements.item.ItemRequirement;
import com.questhelper.requirements.player.SkillRequirement;
import com.questhelper.requirements.quest.QuestRequirement;
import com.questhelper.requirements.util.LogicType;
import com.questhelper.requirements.util.Operation;
import com.questhelper.requirements.var.VarbitRequirement;
import com.questhelper.requirements.zone.Zone;
import com.questhelper.requirements.zone.ZoneRequirement;
import com.questhelper.rewards.ItemReward;
import com.questhelper.rewards.QuestPointReward;
import com.questhelper.rewards.UnlockReward;
import com.questhelper.steps.ConditionalStep;
import com.questhelper.steps.DetailedQuestStep;
import com.questhelper.steps.NpcStep;
import com.questhelper.steps.ObjectStep;
import com.questhelper.steps.QuestStep;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;

// TODO: Some NPC/object/varbit ids aren't in the RuneLite API yet; they come from the local Rev239Gamevals shim, imported here under the gameval names. Remove the shim and these three imports once the API ships them.
import com.questhelper.helpers.quests.thebloodmoonrises.Rev239Gamevals.NpcID;
import com.questhelper.helpers.quests.thebloodmoonrises.Rev239Gamevals.ObjectID;
import com.questhelper.helpers.quests.thebloodmoonrises.Rev239Gamevals.VarbitID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Quest step trigger values, puzzle solutions, and the Vampyrium sub-zones are best-effort and should be double-checked in-game.

public class TheBloodMoonRises extends BasicQuestHelper
{
	// --- Items required / recommended ---
	ItemRequirement combatGear, blisterwoodFlail, food, prayerPotions, staminaPotions, teleportsOutOfVampyrium,
		drakanMedallion, antivenom;

	// --- Steps: Morytania prelude ---
	QuestStep talkToSariusGuile, gatherMyreque, mineTunnel, enterLab, doPlinthPuzzle;

	// --- Steps: Vampyrium traversal + first Castle Drakan infiltration ---
	QuestStep crossSteppingStones, mineRubble, rescueAranei, infiltrateCastle;

	// --- Steps: Return to Morytania campaign ---
	QuestStep regroupAtBurgh;

	// --- Steps: Sangvesti ---
	QuestStep sangvestiKeyHunt, assembleMyrmelKey, fixDrawbridge, gatherAtSangvesti;

	// --- Steps: Castle Drakan puzzles ---
	QuestStep castleClockPuzzle, castleBookPuzzle, castleBasinPuzzle, castleEmblemPuzzle, castleLeverPuzzle, castleMoonDoors;

	// --- Steps: Sugadinti Hideout defence ---
	QuestStep hideoutRepair, hideoutSupplies, hideoutDefence;

	// --- Steps: Boss gauntlet + finish ---
	QuestStep fightSugadintiVitur, fightWyrd, fightLowerniel, fightLowernielFinal, finishQuest;

	// --- Conditions ---
	Requirement inVampyrium, rockMined, araneiSaved, drawbridgeFixed, foundThirdEmblem;

	// --- Zones ---
	Zone vampyrium;

	@Override
	public Map<Integer, QuestStep> loadSteps()
	{
		Map<Integer, QuestStep> steps = new HashMap<>();
		initializeRequirements();
		setupConditions();
		setupSteps();

		// Start at the Icyene Graveyard.
		steps.put(0, talkToSariusGuile);
		steps.put(1, talkToSariusGuile);

		// Rally the Myreque.
		ConditionalStep rallyMyreque = new ConditionalStep(this, gatherMyreque);
		rallyMyreque.addStep(doPlinthPuzzleActive(), doPlinthPuzzle);
		for (int i = 2; i <= 46; i++)
		{
			steps.put(i, rallyMyreque);
		}

		// Mine through the Ivandis tomb tunnel.
		for (int i = 47; i <= 54; i++)
		{
			steps.put(i, mineTunnel);
		}

		// The lab.
		for (int i = 55; i <= 62; i++)
		{
			steps.put(i, enterLab);
		}

		// Cross into Vampyrium and infiltrate Castle Drakan.
		ConditionalStep reachCastle = new ConditionalStep(this, crossSteppingStones);
		reachCastle.addStep(new Conditions(inVampyrium, araneiSaved), infiltrateCastle);
		reachCastle.addStep(new Conditions(inVampyrium, rockMined), rescueAranei);
		reachCastle.addStep(inVampyrium, mineRubble);
		for (int i = 63; i <= 82; i++)
		{
			steps.put(i, reachCastle);
		}

		// Gather the resistance in Sangvesti.
		ConditionalStep doSangvesti = new ConditionalStep(this, sangvestiKeyHunt);
		doSangvesti.addStep(drawbridgeFixed, gatherAtSangvesti);
		for (int i = 83; i <= 99; i++)
		{
			steps.put(i, doSangvesti);
		}

		// Prepare and defend the Sugadinti Hideout.
		ConditionalStep doDefence = new ConditionalStep(this, hideoutRepair);
		doDefence.addStep(new VarbitRequirement(VarbitID.MYQ6_HIDEOUT_DEFENCE_MUSIC, 1, Operation.GREATER_EQUAL), hideoutDefence);
		for (int i = 100; i <= 125; i++)
		{
			steps.put(i, doDefence);
		}

		// Return to Morytania and fight the Wyrd.
		ConditionalStep morytaniaReturn = new ConditionalStep(this, regroupAtBurgh);
		morytaniaReturn.addStep(new VarbitRequirement(VarbitID.MYQ6, 148, Operation.GREATER_EQUAL), fightWyrd);
		for (int i = 126; i <= 157; i++)
		{
			steps.put(i, morytaniaReturn);
		}

		// Final assault on Castle Drakan and the boss gauntlet. Boss order needs in-game confirmation.
		// The quest completes when myq6 hits 178 (endstate), so the helper only ever shows steps up to 177.
		ConditionalStep finalAssault = new ConditionalStep(this, castleEmblemPuzzle);
		finalAssault.addStep(new VarbitRequirement(VarbitID.MYQ6, 176, Operation.GREATER_EQUAL), finishQuest);
		finalAssault.addStep(new VarbitRequirement(VarbitID.MYQ6, 172, Operation.GREATER_EQUAL), fightLowernielFinal);
		finalAssault.addStep(new VarbitRequirement(VarbitID.MYQ6, 168, Operation.GREATER_EQUAL), fightLowerniel);
		finalAssault.addStep(new VarbitRequirement(VarbitID.MYQ6, 160, Operation.GREATER_EQUAL), fightSugadintiVitur);
		finalAssault.addStep(foundThirdEmblem, castleLeverPuzzle);
		for (int i = 158; i <= 177; i++)
		{
			steps.put(i, finalAssault);
		}

		return steps;
	}

	private Requirement doPlinthPuzzleActive()
	{
		return new VarbitRequirement(VarbitID.MYQ6_PLINTH_CONTENTS_IRIANDUL, 1, Operation.GREATER_EQUAL);
	}

	@Override
	protected void setupZones()
	{
		// Whole Vampyrium region. Sangvesti, Castle Drakan and the Sugadinti Hideout all sit within it and
		// aren't separately bounded yet; narrow them in-game if needed.
		vampyrium = new Zone(new WorldPoint(2432, 7680, 0), new WorldPoint(2815, 7935, 3));
	}

	public void setupConditions()
	{
		// In Vampyrium per the quest varbit, or physically within the region as a fallback.
		inVampyrium = new Conditions(LogicType.OR, new VarbitRequirement(VarbitID.IN_VAMPYRIUM, 1), new ZoneRequirement(vampyrium));

		rockMined = new VarbitRequirement(VarbitID.VAMPYRIUM_TRAVERSAL_ROCK_MINED, 1);
		araneiSaved = new VarbitRequirement(VarbitID.VAMPYRIUM_TRAVERSAL_ARANEI_SAVED, 1);
		drawbridgeFixed = new VarbitRequirement(VarbitID.MYQ6_VANESCULA_DRAKAN_FOUND_THE_COG, 1);
		foundThirdEmblem = new VarbitRequirement(VarbitID.CASTLE_DRAKAN_FOUND_THIRD_EMBLEM, 1);
	}

	@Override
	protected void setupRequirements()
	{
		combatGear = new ItemRequirement("Combat gear suited to ~110 combat (melee + ranged switch)", -1, -1).isNotConsumed();
		combatGear.setDisplayItemId(BankSlotIcons.getCombatGear());

		blisterwoodFlail = new ItemRequirement("Blisterwood flail (it is upgraded during the quest)", ItemID.BLISTERWOOD_FLAIL).isNotConsumed();
		blisterwoodFlail.setTooltip("The anti-vampyre weapon for this saga. It becomes the Hallowed flail by the end.");

		food = new ItemRequirement("Food", ItemCollections.GOOD_EATING_FOOD);
		food.setTooltip("Note: Vampyrium also supplies its own food/healing potions, and you cannot withdraw from the bank once inside.");

		prayerPotions = new ItemRequirement("Prayer potions", ItemCollections.PRAYER_POTIONS);
		staminaPotions = new ItemRequirement("Stamina potions", ItemCollections.STAMINA_POTIONS);

		teleportsOutOfVampyrium = new ItemRequirement("A way to teleport out (e.g. Drakan's medallion / Morytania teleports)", ItemID.DRAKANS_MEDALLION).isNotConsumed();
		teleportsOutOfVampyrium.setTooltip("You may leave Vampyrium at any time and resume where you left off. Death there is UNSAFE (Hardcore status is lost) but you keep your items.");

		drakanMedallion = new ItemRequirement("Drakan's medallion", ItemID.DRAKANS_MEDALLION).isNotConsumed();
		antivenom = new ItemRequirement("Antivenom / antipoison", ItemCollections.ANTIVENOMS);
	}

	public void setupSteps()
	{
		// Start
		talkToSariusGuile = new NpcStep(this, NpcID.MYQ6_SARIUS_GUILE_GRAVEYARD, new WorldPoint(3697, 3183, 0),
			"Take the boat from south Burgh de Rott, Meiyerditch or Slepe to the Icyene Graveyard, then speak to Sarius Guile to begin.");
		talkToSariusGuile.addDialogStep("Yes.");

		// Rally the Myreque
		gatherMyreque = new NpcStep(this, NpcID.MYQ6_IVAN_SLEPE,
			"Rally the Myreque with Ivan Strom and Veliaf Hurtz around Morytania (Slepe / Paterdomus). " +
				"Search the Paterdomus and Burgh library bookcases for the lore you need.");
		((NpcStep) gatherMyreque).addAlternateNpcs(NpcID.MYQ6_IVAN_PATERDOMUS);

		doPlinthPuzzle = new DetailedQuestStep(this,
			"Solve the Seven Priestly Warriors plinth puzzle: place each recovered journal on its matching plinth.");
		doPlinthPuzzle.addText("Iriandul Caistlyn = 'From Misthalin to Morytania'.");
		doPlinthPuzzle.addText("Sarl Dunegun = \"Sarl's journal\".");
		doPlinthPuzzle.addText("Derygull Templeton = 'Scruffy notebook'.");
		doPlinthPuzzle.addText("Erysail the Pious = 'Pious proceedings'.");
		doPlinthPuzzle.addText("Friar Twiblick = 'The Life of Friar'.");
		doPlinthPuzzle.addText("Essiandar Gar = \"Essiandar's notes\".");
		doPlinthPuzzle.addText("(Ivandis Seergaze's own writings / the squire's journal complete the set.)");

		mineTunnel = new ObjectStep(this, ObjectID.MYQ6_IVANDIS_TOMB_TUNNEL_BLOCKED_MINE,
			"Mine through the tunnel blockage in the Ivandis tomb with the pickaxe Ivan gave you. (Requires 66 Mining.)");

		enterLab = new NpcStep(this, NpcID.MYQ6_IVAN_LAB,
			"Continue with Ivan and Veliaf in the lab.");

		// Into Vampyrium and Castle Drakan
		crossSteppingStones = new ObjectStep(this, ObjectID.VAMPYRIUM_STEPPING_STONE_SHORTCUT_PILLAR_OP,
			"Step beyond the veil into Vampyrium and cross the stepping stones. Note: no bank withdrawals once inside, and death here is unsafe.");
		mineRubble = new ObjectStep(this, ObjectID.VAMPYRIUM_STEPPING_STONE_NOCLEARED,
			"Mine the rubble blocking the path to clear the way forward.");
		rescueAranei = new NpcStep(this, NpcID.VAMPYRIUM_ARANEI_FOREST_GUIDE_MULTI_WEST,
			"Rescue the trapped Aranei scout, then speak to them. This opens the forest guides.");
		((NpcStep) rescueAranei).addAlternateNpcs(NpcID.VAMPYRIUM_ARANEI_FOREST_GUIDE_MULTI_EAST);
		infiltrateCastle = new DetailedQuestStep(this,
			"Make your way into Castle Drakan. Your allies (Veliaf, Safalaan, Vanescula) are captured and held in the " +
				"castle prison — push through the gallery and entry hall to regroup.");

		// Sangvesti
		sangvestiKeyHunt = new DetailedQuestStep(this,
			"Infiltrate Sangvesti. Find each House key to open its buildings:");
		sangvestiKeyHunt.addText("Jovkai key -> Blacksmith & Basic house.");
		sangvestiKeyHunt.addText("Shadum key -> Shadum manor & Fancy house.");
		sangvestiKeyHunt.addText("Vitur key -> Vitur manor (read the Dusty book to find it).");
		sangvestiKeyHunt.addText("Myrmel key -> Myrmel manor, the Bank & the Pub.");
		assembleMyrmelKey = new DetailedQuestStep(this,
			"Combine the Loop half of key and the Tooth half of key to make the Myrmel key.");
		fixDrawbridge = new ObjectStep(this, ObjectID.MYQ6_SANGVESTI_DRAWBRIDGE_MECHANISM_BROKEN,
			"Use the Old cog on the drawbridge mechanism to repair it.");
		fixDrawbridge.addSubSteps(assembleMyrmelKey, sangvestiKeyHunt);

		gatherAtSangvesti = new NpcStep(this, NpcID.MYQ6_VELIAF_SANGVESTI,
			"Regroup with the resistance in Sangvesti — Veliaf, Safalaan and Vanescula Drakan.");
		((NpcStep) gatherAtSangvesti).addAlternateNpcs(NpcID.MYQ6_SAFALAAN_SANGVESTI, NpcID.MYQ6_VANESCULA_SANGVESTI);

		// Castle Drakan puzzles (solutions need in-game confirmation)
		castleEmblemPuzzle = new DetailedQuestStep(this,
			"Castle Drakan: place the 3 Drakan emblems into the hallway receptacles to open routes, " +
				"and pull the teleporter levers to move between wings.");
		castleClockPuzzle = new ObjectStep(this, ObjectID.CASTLE_DRAKAN_CLOCK_1,
			"Repair and set both grandfather clocks. Rotate the big and small hands to the time clued by the family plaque " +
				"('Aramindus, father to Lowerniel, Ranis, Vanescula and Draynor').");
		((ObjectStep) castleClockPuzzle).addAlternateObjects(ObjectID.CASTLE_DRAKAN_CLOCK_2);
		castleBookPuzzle = new ObjectStep(this, ObjectID.CASTLE_DRAKAN_GILDED_BOOKCASE,
			"Solve the library: arrange the 8 books by their House symbol (see the 'Vampyre Houses' book).");
		castleBasinPuzzle = new ObjectStep(this, ObjectID.CASTLE_DRAKAN_LAB_BASIN,
			"Solve the chemistry puzzle: use the Refiner/Analyser and the colour potions (cloudy grey / weightless black / " +
				"thick red / cold bluish-white) to fill the chapel's smoke, shadow, blood and ice basins.");
		castleLeverPuzzle = new ObjectStep(this, ObjectID.CASTLE_DRAKAN_BUST01_LEVER_UP,
			"Pull the four throne-room bust levers into the correct configuration.");
		castleMoonDoors = new DetailedQuestStep(this,
			"Find the five moon-phase keys (new / half / crescent / gibbous / full) to open their matching moon doors.");
		castleEmblemPuzzle.addSubSteps(castleClockPuzzle, castleBookPuzzle, castleBasinPuzzle, castleMoonDoors);

		// Sugadinti Hideout defence
		hideoutRepair = new ObjectStep(this, ObjectID.MYQ6_ENTRY_HALL_PILLAR_DAMAGED,
			"Prepare the Sugadinti Hideout for the assault: repair the damaged pillars and windows.");
		((ObjectStep) hideoutRepair).addAlternateObjects(ObjectID.MYQ6_ENTRY_HALL_WINDOW_DAMAGED);
		hideoutSupplies = new ObjectStep(this, ObjectID.MYQ6_ENTRY_HALL_HAMMER_CRATE,
			"Take hammers and potions from the supply crates while preparing.");
		((ObjectStep) hideoutSupplies).addAlternateObjects(ObjectID.MYQ6_ENTRY_HALL_POTION_CRATE);
		hideoutDefence = new DetailedQuestStep(this,
			"Survive the four-wave assault. Keep the 'Hideout Integrity' bar up (you have a 5:00 timer) and fight alongside Ivan and Veliaf.",
			combatGear);
		hideoutRepair.addSubSteps(hideoutSupplies);

		// Return to Morytania
		regroupAtBurgh = new NpcStep(this, NpcID.MYQ6_IVAN_BURGH_DE_ROTT,
			"Regroup at Burgh de Rott and rally the counterattack with Ivan, Veliaf and the Aranei. " +
				"Expect to face the Wyrd during this stretch.");

		// Boss gauntlet
		// TODO: confirm whether the Nylocas (including a queen) in the final castle are fought adds or cutscene-only.
		fightSugadintiVitur = new NpcStep(this, NpcID.SUGADINTI_COMBAT,
			"Defeat Sugadinti Vitur (1 HP bar; very high Defence, portal-caster).", combatGear);
		fightWyrd = new NpcStep(this, NpcID.SAFALAAN_WYRD,
			"Defeat the Wyrd (all combat styles; avoid the blood-rain/pool hazards).", combatGear);
		fightLowerniel = new NpcStep(this, NpcID.MYQ6_LOWERNIEL_COMBAT_1,
			"Fight Lowerniel Drakan (phases 1-3). Watch each telegraphed attack and step onto the safe 'dodge' tile to avoid it. " +
				"Kill the blood-wave adds.", combatGear);
		((NpcStep) fightLowerniel).addAlternateNpcs(NpcID.MYQ6_LOWERNIEL_COMBAT_2, NpcID.MYQ6_LOWERNIEL_COMBAT_3);
		fightLowernielFinal = new NpcStep(this, NpcID.MYQ6_LOWERNIEL_FINAL_COMBAT,
			"Final phase: Lowerniel's wing is restored and he enrages (huge Attack) but his Defence halves — push your damage here.", combatGear);
		fightLowerniel.addSubSteps(fightLowernielFinal);

		// Completion (the post-quest Veliaf only exists once myq6 is 178, by which point the quest reads as
		// finished, so this is a generic end-of-fight step rather than an NPC step).
		finishQuest = new DetailedQuestStep(this,
			"Defeat Lowerniel Drakan and watch the ending to complete the quest.");
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		return Arrays.asList(combatGear, blisterwoodFlail, food, prayerPotions);
	}

	@Override
	public List<ItemRequirement> getItemRecommended()
	{
		return Arrays.asList(staminaPotions, teleportsOutOfVampyrium, drakanMedallion, antivenom);
	}

	@Override
	public List<String> getCombatRequirements()
	{
		// TODO: Combat levels are not yet confirmed.
		return Arrays.asList(
			"Sugadinti Vitur (652 HP)",
			"Wyrd (1100 HP)",
			"Lowerniel Drakan (3000 HP, multi-phase, tile-dodge mechanic)");
	}

	@Override
	public List<Requirement> getGeneralRequirements()
	{
		ArrayList<Requirement> req = new ArrayList<>();
		req.add(new QuestRequirement(QuestHelperQuest.SINS_OF_THE_FATHER, QuestState.FINISHED));
		req.add(new QuestRequirement(QuestHelperQuest.A_NIGHT_AT_THE_THEATRE, QuestState.FINISHED));
		req.add(new SkillRequirement(Skill.SLAYER, 74));
		req.add(new SkillRequirement(Skill.WOODCUTTING, 74));
		req.add(new SkillRequirement(Skill.SMITHING, 72));
		req.add(new SkillRequirement(Skill.COOKING, 72));
		req.add(new SkillRequirement(Skill.FLETCHING, 70));
		req.add(new SkillRequirement(Skill.MINING, 66));
		req.add(new SkillRequirement(Skill.HUNTER, 65));
		req.add(new SkillRequirement(Skill.CRAFTING, 64));
		req.add(new SkillRequirement(Skill.HERBLORE, 64));
		req.add(new SkillRequirement(Skill.MAGIC, 57));
		return req;
	}

	@Override
	public QuestPointReward getQuestPointReward()
	{
		return new QuestPointReward(4);
	}

	@Override
	public List<ItemReward> getItemRewards()
	{
		return Arrays.asList(
			new ItemReward("6 x Tome of experience (30,000 XP each, any skill above 70)", Rev239Gamevals.ItemID.MYQ6_XP_TOME, 6),
			new ItemReward("Hallowed flail (Blisterwood flail upgrade)", Rev239Gamevals.ItemID.HALLOWED_FLAIL, 1),
			new ItemReward("Sunspear", Rev239Gamevals.ItemID.SUNSPEAR, 1));
	}

	@Override
	public List<UnlockReward> getUnlockRewards()
	{
		return Arrays.asList(
			new UnlockReward("Access to Vampyrium and its skilling activities (Stymphike Hunter, Leechfin Fishing, Bloodwood Woodcutting, Venator Slayer)"),
			new UnlockReward("Access to the Maggot King boss"),
			new UnlockReward("The Sunspear special attack: Seeking Lunge"));
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();

		allSteps.add(new PanelDetails("Rallying the Myreque",
			Arrays.asList(talkToSariusGuile, gatherMyreque, doPlinthPuzzle, mineTunnel, enterLab), combatGear, blisterwoodFlail));

		allSteps.add(new PanelDetails("Into Vampyrium & Castle Drakan",
			Arrays.asList(crossSteppingStones, mineRubble, rescueAranei, infiltrateCastle), combatGear, food));

		allSteps.add(new PanelDetails("Sangvesti",
			Arrays.asList(sangvestiKeyHunt, assembleMyrmelKey, fixDrawbridge, gatherAtSangvesti)));

		allSteps.add(new PanelDetails("Defending the Hideout",
			Arrays.asList(hideoutRepair, hideoutSupplies, hideoutDefence), combatGear, food, prayerPotions));

		allSteps.add(new PanelDetails("Return to Morytania",
			Arrays.asList(regroupAtBurgh, fightWyrd), combatGear, blisterwoodFlail, food, prayerPotions));

		allSteps.add(new PanelDetails("The Final Assault",
			Arrays.asList(castleEmblemPuzzle, castleClockPuzzle, castleBookPuzzle, castleBasinPuzzle, castleLeverPuzzle, castleMoonDoors,
				fightSugadintiVitur, fightLowerniel, fightLowernielFinal, finishQuest), combatGear, blisterwoodFlail, food, prayerPotions));

		return allSteps;
	}
}
