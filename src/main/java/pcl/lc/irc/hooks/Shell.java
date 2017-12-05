/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.DiceRoll;
import pcl.lc.utils.Helper;
import pcl.lc.utils.Item;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Shell extends AbstractListener {
	private Command local_command;
	int hitChance = 40; //percentage (out of 100)

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command, "Shell a target or random user. " + hitChance + "% hit chance.");
	}

	private void initCommands() {
		local_command = new Command("shell", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				DiceRoll roll = Helper.rollDice("1d100");

				Item item = Inventory.getRandomItem(false);
				if (item != null) {
					String user = params;
					String userSecondary = Helper.getRandomUser(event);
					String userTertiary = Helper.getRandomUser(event);
					if (user == "")
						user = Helper.getRandomUser(event);
					else if (!Helper.doInterractWith(user))
          {
            Helper.sendAction(target, "kicks " + nick + " into space.");
            return;
          }
					int splash = 2;
					int itemDamage = 0;
					String dust = "";
					String strike = "Seems it was a dud...";
					try {
						if (roll != null && roll.getSum() < hitChance) {
							strike = "It strikes " + user + ". They take " + Helper.rollDice("2d10").getSum() + " damage. " + userSecondary + " and " + userTertiary + " take " + Helper.rollDice("1d10").getSum() + " and " + Helper.rollDice("1d10").getSum() + " splash damage respectively.";
							itemDamage = 1;
						} else {
							strike = "It strikes the ground near " + user + ", " + userSecondary + " and " + userTertiary + ". They each take " + Helper.rollDice("1d10").getSum() + ", " + Helper.rollDice("1d10").getSum() + " and " + Helper.rollDice("1d10").getSum() + " splash damage respectively.";
							itemDamage = 2;
						}
					} catch (NullPointerException ignored) {}
					Helper.AntiPings = Helper.getNamesFromTarget(target);
					Helper.sendAction(target, "loads " + item.getName(false) + " into a shell and fires it. " + strike);
					dust = item.damage(itemDamage,false, true, true);
					if (dust != "") {
						Helper.AntiPings = Helper.getNamesFromTarget(target);
						Helper.sendAction(target, dust);
					}
				} else {
					Helper.AntiPings = Helper.getNamesFromTarget(target);
					Helper.sendAction(target, "found nothing to fire load into the shell...");
				}
			}
		};
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		target = Helper.getTarget(event);
		local_command.tryExecute(command, nick, target, event, copyOfRange);
	}
}