  url = "http://3dviewer.neurofly.de/";
  commands = Menus.getCommands();
  found = commands!=null && commands.get("3D_Viewer")==null;
  if (!found)
     (new BrowserLauncher()).openURL(url);
  else {
     ed = new Editor();
     ed.setSize(500, 150);
     ed.create("About ImageJ 3D Viewer", "\n  ImageJ 3D Viewer not found. "
       +"You can download it from\n \n     " + url);
  }
