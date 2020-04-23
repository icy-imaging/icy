// This tool creates a circular selection centered
// where you click and measures and labels it.
// Double click on the tool icon to change the radius.

    var radius = 10;

    macro "Spot Measurement Tool - C00bO11cc" {
        saveSettings;
        setOption("Add to overlay", true);
        getCursorLoc(x, y, z, flags);
        makeOval(x-radius, y-radius, radius*2, radius*2);
        run("Measure");
        restoreSettings;
    }

  macro "Measure And Label Tool Options" {
      radius = getNumber("Radius (pixels):", radius);
  }
