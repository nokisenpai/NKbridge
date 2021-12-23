# NKblank

A blank plugin to create a new plugin under the NK standard

## Create a new plugin based on NKblank

1. Clone NKblank repository `git clone <url>`
2. Rename the project folder as you want
3. Delete the .git folder in project
4. Open the project with IntelliJ
5. Go to `File > Project Structure... > Modules` and rename NKblank module as you want
6. Open the pom.xml and rename the `artifactId` and `name` as you want
8. Rename NKblank package with the same name in pom.xml
9. Create a new repository in git
10. In IntelliJ run `git init`
11. Then `git add .`
12. Then `git commit -m “Initial commit”`
13. Then `git remote add origin <your_git_repo_url>`
14. Then `git push -u origin master`

## How to use

### Add new command

1. Create a classic plugin command
2. Add the command info in `plugin.yml` file
3. [NEW] Add the command name and a command instance in `CommandRegister` class

       private static Map<String, CommandExecutor> setCommands()  
       {  
           Map<String, CommandExecutor> commands = new HashMap<>();
           commands.put(NKData.PLUGIN_NAME, new RootCmd());
           commands.put("mycommand", new MyCommand()); //Your new command
            
           return commands;
       }

### Add new command completer

1. Create a classic plugin command completer
2. Be sure that the command info are in `plugin.yml` file
3. [NEW] Add the command name and a command completer instance in `CompleterRegister` class

        private static Map<String, TabCompleter> setCompleters()  
        {  
           Map<String, TabCompleter> completers= new HashMap<>();
           completers.put(NKData.PLUGIN_NAME, new RootCompleter());
           completers.put("mycommand", new MyCompleter()); //Your new command completer
            
           return completers;
       }

### Add new listener

1. Create a classic listener
2. [NEW] Add a listener instance in `ListenerRegister` class

        private static List<Listener> setListeners() 
        {  
           List<Listener> listeners = new ArrayList<>();
           listeners.add(new PlayerListener());
           listeners.add(new MyListener()); //Your new listener
           
           return listeners;
       }

### [NEW] Add new sub-command to main plugin command (plugin name)

1. Create a sub-command class using `SubCommand interface`
2. Add the sub-command as new switch case in `RootCmd` class

           switch(args[0])
           {
               case "reload":
                   return new Reload().execute(sender, args);
               case "mysubcommand": //Your sub command
                   return new MySubCommand().execute(sender, args);
               default:
                   sender.sendMessage(Usages.ROOT_CMD.toString());
           }