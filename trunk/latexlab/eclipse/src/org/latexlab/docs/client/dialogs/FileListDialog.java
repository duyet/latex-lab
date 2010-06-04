package org.latexlab.docs.client.dialogs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;

import org.latexlab.docs.client.commands.SystemListDocumentsCommand;
import org.latexlab.docs.client.commands.SystemStarDocumentCommand;
import org.latexlab.docs.client.commands.SystemUnstarDocumentCommand;
import org.latexlab.docs.client.events.AsyncInstantiationCallback;
import org.latexlab.docs.client.events.CommandEvent;
import org.latexlab.docs.client.events.CommandHandler;
import org.latexlab.docs.client.gdocs.DocumentServiceEntry;
import org.latexlab.docs.client.resources.icons.Icons;
import org.latexlab.docs.client.widgets.ExplorerTree;
import org.latexlab.docs.client.widgets.ExplorerTree.StarHandler;

/**
 * A dialog window displaying the user's documents.
 */
public class FileListDialog extends Dialog {

  protected static FileListDialog instance;

  /**
   * Retrieves the single instance of this class, using asynchronous instantiation.
   * 
   * @param handler the command handler.
   * @param cb the asynchronous instantiation callback.
   */
  public static void get(final CommandHandler handler,
	    final AsyncInstantiationCallback<FileListDialog> cb) {
	GWT.runAsync(new RunAsyncCallback() {
		@Override
		public void onFailure(Throwable reason) {
		  cb.onFailure(reason);
		}
		@Override
		public void onSuccess() {
	      if (instance == null) {
	        instance = new FileListDialog();
	        instance.addCommandHandler(handler);
	      }
		  cb.onSuccess(instance);
		}
	});
  }
  
  /**
   * Causes the code for this class to be loaded.
   */
  public static void prefetch() {
	GWT.runAsync(new RunAsyncCallback() {
		@Override
		public void onFailure(Throwable reason) { }
		@Override
		public void onSuccess() {
		  new AboutDialog();
		}
	});
  }
  
  private DocumentServiceEntry[] entries;
  private VerticalPanel leftPanel, rightPanel;
  private ScrollPanel linksPanel, documentsPanel;
  private TabBar tabs;
  
  /**
   * Constructs a dialog window displaying the user's documents.
   */
  protected FileListDialog() {
    super("Your Documents", true);
    addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        hide();
      }
    });
    documentsPanel = new ScrollPanel(new VerticalPanel());
    leftPanel = new VerticalPanel();
    leftPanel.setWidth("100px");
    leftPanel.setVerticalAlignment(VerticalPanel.ALIGN_TOP);
    rightPanel = new VerticalPanel();
    rightPanel.setVerticalAlignment(VerticalPanel.ALIGN_TOP);
    rightPanel.add(documentsPanel);
    FlexTable table = new FlexTable();
    table.insertRow(0);
    table.insertCell(0, 0);
    table.insertCell(0, 1);
    table.setWidget(0, 0, leftPanel);
    table.getCellFormatter().setVerticalAlignment(1, 0, VerticalPanel.ALIGN_TOP);
    table.setWidget(0, 1, rightPanel);
    table.getCellFormatter().setVerticalAlignment(1, 1, VerticalPanel.ALIGN_TOP);
    setContentWidget(table);
    buildTabBar();
    buildLinks();
    Window.addResizeHandler(new ResizeHandler(){
      public void onResize(ResizeEvent event) {
        resize();
        if (isShowing()) center();
      }
    });
  }
  

  /**
   * Builds the dialog's link panel.
   */
  private void buildLinks() {
    VerticalPanel panel = new VerticalPanel();
    panel.setStylePrimaryName("lab-Explorer-Links");
    mainPanel.getFlexCellFormatter().setStyleName(1, 0, ""); //cancel default style
    Anchor refreshLink = new Anchor("Refresh");
    refreshLink.addClickHandler(new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
		  event.preventDefault();
		  event.stopPropagation();
		  loadEntries(false);
		}
    });
    Anchor newDocumentLink = new Anchor("New Document", "/docs", "_blank");
    Anchor acLink = new Anchor("Google Access Control", "https://www.google.com/accounts/IssuedAuthSubTokens", "_blank");
    Anchor docsLink = new Anchor("Google Documents", "http://docs.google.com/", "_blank");
    panel.add(new HTML("<br /><b>Actions</b>"));
    panel.add(refreshLink);
    panel.add(newDocumentLink);
    panel.add(new HTML("<br /><b>Links</b>"));
    panel.add(acLink);
    panel.add(docsLink);
    linksPanel = new ScrollPanel(panel);
    leftPanel.add(linksPanel);
  }
  
  /**
   * Builds the dialog's tab bar.
   */
  private void buildTabBar() {
    tabs = new TabBar();
    tabs.addTab("Starred Documents");
    tabs.addTab("All Documents");
    tabs.selectTab(0);
    tabs.setWidth("100%");
    tabs.addSelectionHandler(new SelectionHandler<Integer>(){
      public void onSelection(SelectionEvent<Integer> event) {
        showEntries();
      }
    });
    setTopWidget(tabs);
  }
  
  /**
   * Requests a document list refresh by firing the appropriate command event.
   * 
   * @param useCache whether to use a document cache, when available.
   */
  private void loadEntries(boolean useCache) {
    documentsPanel.clear();
    rightPanel.setStylePrimaryName("lab-Loading");
    CommandEvent.fire(this, new SystemListDocumentsCommand(useCache));
  }
  
  /**
   * Resizes the dialog window to match the browser window size.
   */
  private void resize() {
    int targetHeight = Window.getClientHeight() - 180;
    rightPanel.setPixelSize(500, targetHeight);
    documentsPanel.setPixelSize(500, targetHeight);
    linksPanel.setPixelSize(120, targetHeight);
  }
  
  /**
   * Displays a document list.
   * 
   * @param documents the documents to display
   */
  public void setEntries(DocumentServiceEntry[] entries) {
    this.entries = entries;
  }
  
  /**
   * Makes the dialog window visible. Resets the tab selection to
   * display the default documents view and triggers a refresh.
   */
  @Override
  public void show() {
    super.show();
    resize();
    if (entries == null) {
      loadEntries(true);
    }
  }
  
  /**
   * Displays entries according to the selected view.
   */
  public void showEntries() {
	rightPanel.clear();
	rightPanel.setStyleName("");
	switch(tabs.getSelectedTab()) {
	  case 0:
		showQuickView();
	    break;
	  case 1:
		showExplorerView();
		break;
	}
  }
  
  /**
   * Displays an explorer view of all entries.
   */
  private void showExplorerView() {
	if (entries == null) {
	  return;
	}
    ExplorerTree tree = new ExplorerTree(false, false, new StarHandler(){
		@Override
		public void onStar(String id) {
		  CommandEvent.fire(FileListDialog.this, new SystemStarDocumentCommand(id));
		}
		@Override
		public void onUnstar(String id) {
		  CommandEvent.fire(FileListDialog.this, new SystemUnstarDocumentCommand(id));
		}
    });
    tree.setEntries(entries);
    documentsPanel = new ScrollPanel(tree);
    rightPanel.add(documentsPanel);
    resize();
  }
  
  /**
   * Displays a quick view of starred entries.
   */
  private void showQuickView() {
    if (entries == null) {
	  return;
	}
    VerticalPanel panel = new VerticalPanel();
    panel.setStylePrimaryName("lab-Explorer-Documents");
    int totalEntries = 0;
    for (DocumentServiceEntry entry : entries) {
      if (!entry.isStarred()) {
        continue;
      }
      final String id = entry.getDocumentId();
      FlexTable docTable = new FlexTable();
      docTable.insertRow(0);
      docTable.insertCell(0, 0);
      docTable.insertCell(0, 1);
      docTable.insertCell(0, 2);
      docTable.insertRow(1);
      docTable.insertCell(1, 0);
      docTable.insertCell(1, 1);
      docTable.insertCell(1, 2);
      docTable.insertRow(2);
      docTable.insertCell(2, 0);
      docTable.insertCell(2, 1);
      docTable.insertCell(2, 2);
      docTable.setStylePrimaryName("lab-Explorer-Document");
      Anchor link = new Anchor();
      link.setText(entry.getTitle());
      link.setTarget("_blank");
      if (entry.getType().equalsIgnoreCase("document")) {
        link.setHref("/docs?docid=" + entry.getDocumentId());
      }
      ToggleButton star = new ToggleButton(
          Icons.editorIcons.StarEmpty().createImage(),
          Icons.editorIcons.StarFull().createImage());
      star.setDown(entry.isStarred());
      star.addClickHandler(new ClickHandler(){
        public void onClick(ClickEvent event) {
          ToggleButton btn = (ToggleButton)event.getSource();
          if (btn.isDown()) {
            CommandEvent.fire(FileListDialog.this, new SystemStarDocumentCommand(id));
          } else {
            CommandEvent.fire(FileListDialog.this, new SystemUnstarDocumentCommand(id));
          }
        }
      });
      Label info = new Label(entry.getEdited().toString() + " by " + entry.getEditor());
      info.setStylePrimaryName("lab-Explorer-Document-Info");
      docTable.setWidget(0, 0, star);
      docTable.setWidget(0, 1, Icons.editorIcons.Document().createImage());
      docTable.setWidget(0, 2, link);
      docTable.setWidget(1, 2, info);
      if (entry.getFolders().length > 0) {
        Label folderLabel = new Label(entry.getFolders()[0]);
        folderLabel.setStylePrimaryName("lab-Explorer-Document-Folder");
    	HorizontalPanel folderPanel = new HorizontalPanel();
    	folderPanel.add(folderLabel);
        docTable.setWidget(2, 2, folderPanel);
      }
      panel.add(docTable);
      totalEntries++;
    }
    if (totalEntries == 0) {
      panel.add(new Label("You have no starred documents."));
    }
    documentsPanel = new ScrollPanel(panel);
    rightPanel.add(documentsPanel);
    resize();
  }
  
}
