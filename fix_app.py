import os

# --- File Contents ---

# This string contains only the missing styles for the Trading Flow screens.
trading_flow_styles = r"""

/* --- FIX: Added Missing Styles for Trading Flow --- */
.checklist-item {
    -fx-background-color: -frame-bg-color;
    -fx-border-color: -border-color;
    -fx-border-radius: 8;
    -fx-background-radius: 8;
    -fx-text-fill: -soft-white-text;
    -fx-font-family: "Fira Code Regular";
    -fx-font-size: 14px;
    -fx-padding: 15;
    -fx-alignment: CENTER_LEFT;
    -fx-graphic-text-gap: 10;
}
.checklist-item .ikonli-font-icon {
    -fx-icon-color: transparent;
    -fx-icon-size: 18;
}
.checklist-item:selected {
    -fx-border-color: -positive-color;
    -fx-text-fill: -dim-white-text;
    -fx-font-family: "Fira Code Bold";
}
.checklist-item:selected .ikonli-font-icon {
    -fx-icon-color: -positive-color;
}
.setup-review-item {
    -fx-background-color: -frame-bg-color;
    -fx-border-color: -border-color;
    -fx-border-radius: 8;
    -fx-background-radius: 8;
    -fx-text-fill: -soft-white-text;
    -fx-font-family: "Fira Code Regular";
    -fx-font-size: 14px;
    -fx-padding: 15;
    -fx-alignment: CENTER;
}
.setup-review-item:hover {
    -fx-border-color: -button-color;
}
.setup-review-item:disabled {
    -fx-opacity: 1.0;
}
.setup-review-item.acknowledged {
    -fx-background-color: -positive-color;
    -fx-text-fill: white;
    -fx-font-family: "Fira Code Bold";
    -fx-border-color: -positive-color;
}
"""

# --- Python Script Logic ---

def apply_fixes():
    """
    Appends the missing trading flow styles to the end of the styles.css file.
    """
    project_root = os.getcwd()
    
    # Define file path
    styles_css_path = os.path.join(project_root, "src", "main", "resources", "com", "tdf", "styles.css")
    
    try:
        # Open the file in append mode ('a') to add the new style without deleting existing content
        with open(styles_css_path, 'a', encoding='utf-8') as f:
            f.write(trading_flow_styles)
        print(f"Successfully fixed trading flow styles in: {styles_css_path}")

    except IOError as e:
        print(f"An error occurred while writing to the file: {e}")
    except Exception as e:
        print(f"An unexpected error occurred: {e}")

if __name__ == "__main__":
    apply_fixes()