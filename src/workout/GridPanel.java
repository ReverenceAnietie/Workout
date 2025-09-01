package workout;

import javax.swing.*;
import java.awt.*;

public class GridPanel extends JPanel {

    public GridPanel(LayoutManager layout) {
        super(layout);
        initStyle();
    }

    public GridPanel() {
        super();
        initStyle();
    }

    private void initStyle() {
        // Apply consistent styling for all GridPanels
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setBackground(UIUtils.BACKGROUND); // use your theme background color
    }
}
