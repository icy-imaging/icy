// ButtonBar
//
// This example macro set adds six buttons to the Image tool bar.
// Information about tool macros is available at
// http://rsb.info.nih.gov/ij/developer/macro/macros.html#tools

    macro "A Button Action Tool - C059T3e16A" {handleClick("A")}
    macro "B Button Action Tool - C059T3e16B" {handleClick("B")}
    macro "C Button Action Tool - C059T3e16C" {handleClick("C")}
    macro "D Button Action Tool - C059T3e16D" {handleClick("D")}
    macro "E Button Action Tool - C059T3e16E" {handleClick("E")}
    macro "F Button Action Tool - C059T3e16F" {handleClick("F")}

    function handleClick(button) {
        showMessage("Button Bar", "The \""+button+"\" Button was pressed");
    }
