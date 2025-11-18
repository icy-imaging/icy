// This tool is a wand tool that also runs the Measure command

    macro "Wand Auto-Measure Tool -C00b-Lee22-o2244" {
        getCursorLoc(x, y, z, flags);
        doWand(x,y);
        if (selectionType!=0)
            run("Measure");
    }
