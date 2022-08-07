package net.dollmar.web.apicatalogue.utils;

import java.io.Console;
import java.util.Arrays;

import net.dollmar.web.apicatalogue.data.Users;

public class CreateUser {


	public static void main(String[] args) {

		System.out.println("\n*** APIWorkbench: You are about to create a new user ***\n");

		Console console = System.console();
		if (console != null) {
			Users u = new Users();

			String userName = console.readLine("Enter user name: ");
			if (u.existUser(userName)) {
				String choice = console.readLine(String.format("   *** User '%s' already exist. Overwirte [Y/N]: ", userName));
				if (!choice.startsWith("Y") && !choice.startsWith("y")) {
					return;
				}
			}
			char[] password = console.readPassword("Enter password: ");

			try {
				u.createUser(userName, password);

				Arrays.fill(password, ' ');
			}
			catch (Exception e) {
				System.err.println(String.format("ERROR: Failed to create new user [Reason: %s]", e.getMessage()));
			}
		}
		else {
			System.err.println("ERROR: No Console available.");
		}
	}
}