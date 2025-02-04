package we.devs.opium.client.modules.combat;
/**
import we.devs.opium.api.manager.module.Module;
import we.devs.opium.api.manager.module.RegisterModule;
import we.devs.opium.client.values.impl.ValueBoolean;
import we.devs.opium.client.values.impl.ValueEnum;
import we.devs.opium.client.values.impl.ValueNumber;

@RegisterModule(name = "AutoCrystal", tag = "Auto Crystal", description = "Automatically places and breaks Crystal", category = Module.Category.COMBAT)
public class ModuleAutoCrystal {

    // AntiCheat Settings
    public ValueBoolean multitask = new ValueBoolean("Multitask", "Multitask", "Perform multiple actions simultaneously.", true);
    public ValueEnum yawstep = new ValueEnum("YawStep", "Yaw Step","When to perform yaw stepping.", List.of("Off", "Full", "Break", "Place"), 0);
    public ValueNumber yawstepAngle = new ValueNumber("YawStepAngle", "Yaw Step Angle", "Maximum angle to rotate per tick.",  45, 1, 180);
    public ValueBoolean strictDirection = new ValueBoolean("StrictDirection", "StrictDirection", "Bypass stricter direction checks.", false);
    public ValueEnum swap = new ValueEnum("Swap", "Swap", "Method of item swapping.", List.of("None", "Normal", "Silent", "Alternative"), 0);
    public ValueBoolean antiWeakness = new ValueBoolean("AntiWeakness", "Anti Weakness", "Swap to a tool to bypass weakness effect.", true);

    // Place Settings
    public ValueBoolean place = new ValueBoolean("Place", "Place", "Toggle crystal placing.", true);
    public ValueNumber placeRange = new ValueNumber("PlaceRange", "Place Range", "Maximum range to place crystals.", 4.0, 0.0, 6.0);
    public ValueNumber placeWallRange = new ValueNumber("PlaceWallRange", "Place Wall Range", "Maximum range to place crystals through walls.", 4.0, 0.0, 6.0);
    public ValueNumber placeDelay = new ValueNumber("PlaceDelay", "Place Delay", "Delay between crystal placements in Milliseconds.", 50, 0, 1000);
    public ValueEnum placeSwing = new ValueEnum("Swing", "Swing", "Method of swinging when placing.", List.of("Vanilla", "None", "Packet"), 0);
    public ValueBoolean placeRotate = new ValueBoolean("Rotate", "Rotate", "Rotate when placing.", true);
    public ValueBoolean oldPlace = new ValueBoolean("OldPlacements", "Old Placements", "Use the Pre 1.13 placement system.", false);

    // Break Settings
    public ValueBoolean breakToggle = new ValueBoolean("Break", "Break", "Toggle crystal breaking.", true);
    public ValueNumber breakRange = new ValueNumber("BreakRange", "Break Range", "Maximum range to break crystals.", 4.0, 0.1, 6.0);
    public ValueNumber breakWallRange = new ValueNumber("BreakWallRange", "Break Wall Range","Maximum range to break crystals through walls.", 4.0, 0.1, 6.0);
    public ValueNumber breakDelay = new ValueNumber("BreakDelay", "Break Delay", "Delay between crystal breaks in Milliseconds.", 50, 0, 1000);
    public ValueBoolean breakRotate = new ValueBoolean("Rotate", "Rotate", "Rotate when breaking.", true);
    public ValueEnum cancelMode = new ValueEnum("CancelMode", "Cancel Mode", "Mode to cancel block breaking.", List.of("Instant", "Sound", "None"), 0);
    public ValueEnum breakSwing = new ValueEnum ("Swing", "Swing", "Method of swinging when breaking.", List.of("Vanilla", "None", "Packet"), 0);
    public ValueNumber breakTicksExisted = new ValueNumber("TicksExisted", "Ticks Existed","Minimum ticks a crystal must exist before breaking.", 1, 1, 10);
    public ValueEnum inhibit = new ValueEnum("Inhibit", "Inhibition mode for crystal breaking.", List.of("Strict", "Strong", "None"), 0);

    // Render Settings
    public SeparatorSetting blockRenderSeparator = new SeparatorSetting("Placement Render Settings");
    public ColorSetting blockColorFill = new ColorSetting("Color Fill", "Color used to fill the block.", new Color(0, 255, 128,60), true, false, false);
    public ColorSetting blockColorOutline = new ColorSetting("Color Outline", "Color used for the block outline.", new Color(0, 255, 128,150), true, false, false);
    public NumberSetting blockLineWidth = new NumberSetting("Line Width", "Thickness of the block outline.", 1.0, 1.0, 3.0, 0.1);
    public BooleanSetting blockFade = new BooleanSetting("Fade", "Enable fade effect for placement rendering.", true);
    public NumberSetting blockFadeSpeed = new NumberSetting("Fade Speed", "Speed at which the fade effect occurs.", 5, 1, 10, 1);
    public BooleanSetting blockFadeOnlyLast = new BooleanSetting("Fade Only Last Block", "Only fade the last placement location.", false);
    public BooleanSetting blockShrink = new BooleanSetting("Shrink", "Enable shrink effect for placement rendering.", false);
    public NumberSetting blockShrinkSpeed = new NumberSetting("Shrink Speed", "Speed at which the shrink effect occurs.", 5, 1, 10, 1);
    public BooleanSetting blockDamageText = new BooleanSetting("Damage Text", "Display damage text on blocks.", true);
    public NumberSetting blockDamageTextScale = new NumberSetting("Scale", "Scale of the damage text.", 1, 1, 10, 1);
    public BooleanSetting blockInterpolateBox = new BooleanSetting("Interpolate Block Box", "Interpolation for placement renders.", false);
    public NumberSetting blockInterpolateSpeed = new NumberSetting("Interpolate Speed", "Speed of interpolation for placement renders.", 5, 1, 10, 1);
    public BooleanSetting blockInterpolateProportional = new BooleanSetting("Proportional", "Make interpolation proportional to the distance between placements.", false);

    public SeparatorSetting targetRenderSeparator = new SeparatorSetting("Target Render Settings");
    public BooleanSetting targetBox = new BooleanSetting("Box", "Render a box for current target.", true);
    public ColorSetting targetBoxColorFill = new ColorSetting("Color Fill", "Color used to fill the target box.", new Color(255, 0, 251,60), true, false, false);
    public ColorSetting targetBoxColorOutline = new ColorSetting("Color Outline", "Color used for the target box outline.", new Color(255, 0, 251,150), true, false, false);
    public NumberSetting targetBoxLineWidth = new NumberSetting("Line Width", "Thickness of the target box outline.", 1.0, 1.0, 3.0, 0.1);

    public SeparatorSetting tracerRenderSeparator = new SeparatorSetting("Tracer Render Settings");
    public BooleanSetting targetTracer = new BooleanSetting("Tracer", "Tracer lines to target.", true);
    public ColorSetting targetTracerColor = new ColorSetting("Color", "Color of the tracer line.", Color.WHITE, true, false, false);
    public NumberSetting targetTracerLineWidth = new NumberSetting("Line Width", "Thickness of the tracer line.", 1.0, 1.0, 3.0, 0.1);

    // Targets Settings
    public BooleanSetting targetsPlayers = new BooleanSetting("Players", "Players as targets.", true);
    public BooleanSetting targetsPlayersIgnoreNaked = new BooleanSetting("Ignore Naked", "Ignore naked players.", false);
    public BooleanSetting targetsMonsters = new BooleanSetting("Monsters", "Monsters as targets.", true);
    public BooleanSetting targetsAnimals = new BooleanSetting("Animals", "Animals as targets.", false);

    // Pause Settings
    public BooleanSetting pauseEat = new BooleanSetting("Eat", "Pause module while eating.", false);
    public BooleanSetting pauseHealth = new BooleanSetting("Health", "Pause module based on health.", false);
    public NumberSetting pauseHealthValue = new NumberSetting("Health Value", "Health threshold to pause at.", 10, 1, 36, 0.1);
    public BooleanSetting pauseMining = new BooleanSetting("Mining", "Pause module while mining.", false);

    // Targeting Settings
    public NumberSetting targetingRange = new NumberSetting("Target Range", "Range for selecting targets.", 12.0, 1.0, 20.0, 0.1);
    public EnumSetting targetingMode = new EnumSetting("Targeting Mode", "Method for selecting target", List.of("Damage", "Range", "Health"), 0);
    public NumberSetting targetingMinDamagePlace = new NumberSetting("Min Place Damage", "Minimum damage required to place.", 6.0, 0.1, 20.0, 0.1);
    public NumberSetting targetingMinDamageBreak = new NumberSetting("Min Break Damage", "Minimum damage required to break.", 6.0, 0.1, 20.0, 0.1);
    public SeparatorSetting targetingProtectSeparator = new SeparatorSetting("Protect");
    public EnumSetting targetingSelfProtect = new EnumSetting("Self", "Protect mode for self-targeting.", List.of("Pop", "Kill", "Both", "None"), 0);
    public EnumSetting targetingFriendProtect = new EnumSetting("Friend", "Protect mode for friend-targeting.", List.of("Pop", "Kill", "Both", "None"), 0);
    public BooleanSetting targetingSacrifice = new BooleanSetting("Sacrifice", "Sacrifice own totems when able to pop enemy.", false);
    public SeparatorSetting targetingSafetySeparator = new SeparatorSetting("Safety");
    public BooleanSetting targetingSafetyApplyBalance = new BooleanSetting("Apply Balance", "Apply damage balance.", true);
    public NumberSetting targetingSafetyDamageBalance = new NumberSetting("Damage Balance", "Balance factor for damage.", 0.8, 0.1, 3.0, 0.1);
    public SeparatorSetting targetingFacePlaceSeparator = new SeparatorSetting("FacePlace");
    public NumberSetting targetingFaceplaceMinDamage = new NumberSetting("FacePlace Min Damage", "Minimum damage for face placing.", 5.0, 0.1, 20.0, 0.1);
    public NumberSetting targetingFaceplaceDelay = new NumberSetting("FacePlace Delay", "Delay for face placing in ticks.", 1, 0, 4, 0.05);
    public NumberSetting targetingFaceplaceHealth = new NumberSetting("FacePlace Health", "Health threshold for face placing.", 10.0, 0.1, 20.0, 0.1);
    public NumberSetting targetingFaceplaceArmorDurabilityPercent = new NumberSetting("Armor Durability Percent", "Minimum armor durability percentage.", 15, 0, 100, 1);
    public BindSetting targetingFaceplaceOverrideBind = new BindSetting("FacePlace Override Bind", "Key to override face placing.", GLFW.GLFW_KEY_UNKNOWN);

    // Calculation Settings
    public SeparatorSetting calculationPredictSeparator = new SeparatorSetting("Predict Settings");
    public BooleanSetting calcPredictSelf = new BooleanSetting("Self", "Enable self prediction.", true);
    public NumberSetting calcPredictSelfTicks = new NumberSetting("Self Ticks", "Number of ticks for self prediction.", 10, 1, 20, 1);
    public BooleanSetting calcPredictEnemy = new BooleanSetting("Enemy", "Enable enemy prediction.", true);
    public NumberSetting calcPredictEnemyTicks = new NumberSetting("Enemy Ticks", "Number of ticks for enemy prediction.", 10, 1, 20, 1);
    public BooleanSetting calcThreaded = new BooleanSetting("Threaded", "Enable threaded calculations.", false);
    public BooleanSetting calcTerrainIgnore = new BooleanSetting("Terrain Ignore", "Ignore terrain in calculations.", false);
}*/