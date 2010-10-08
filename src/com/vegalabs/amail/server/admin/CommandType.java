package com.vegalabs.amail.server.admin;

import com.vegalabs.general.server.command.Command;


public enum CommandType {
	SEND_EMAIL(SendEmail.class),
//	LOAD_CONTACTS_AND_CONTENT(LoadContactsAndContent.class),
//	LOAD_CONTACTS(LoadContacts.class),
	IMPORT_CONTACTS(ImportContacts.class);

  private Class<? extends Command> clazz = null;

  CommandType(Class<? extends Command> clazz) {
    this.clazz = clazz;
  }

  public Class<? extends Command> getClazz() {
    return clazz;
  }

  public static CommandType valueOfIngoreCase(String name) {
    return CommandType.valueOf(name.toUpperCase());
  }
}
