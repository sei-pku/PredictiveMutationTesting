package edu.pku.sei.metric.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import edu.pku.sei.metric.Activator;
import edu.pku.sei.metric.Message;
import edu.pku.sei.metric.source.AbstractMetricElement;
import edu.pku.sei.metric.source.Cache;
import edu.pku.sei.metric.xml.XMLExporter;

/**
 * The view to display metric results
 * 
 * @author liushi07
 * 
 */
public class MetricsView extends ViewPart implements ISelectionListener {
		
	/* Menu/Column names to display */
	private String textCleanCache = Message.getString("textCleanCache");
	private String textExportXML = Message.getString("textExportXML");
	private String textImportXML = Message.getString("textImportXML");
	private String textMetricResult = Message.getString("textMetricResult");

	private Composite tablePage;

	private Composite cards;

	private StackLayout pageSelector;

	private MetricsResultTable table;

	private IMemento memento;

	private IAction cleanCache;

	private IAction exportXML;

	private IAction importXML;

	private AbstractMetricElement rootSelection;

	private Logger logger = Logger.getLogger(MetricsView.class.getName());

	@Override
	public void createPartControl(Composite parent) {

		Composite c = new Composite(parent, SWT.NONE);
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		c.setLayout(new GridLayout(1, false));
		cards = new Composite(c, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		pageSelector = new StackLayout();
		cards.setLayoutData(data);
		cards.setLayout(pageSelector);
		tablePage = new Composite(cards, SWT.NONE);
		tablePage.setLayout(new GridLayout(1, false));
		createTable(tablePage);
		pageSelector.topControl = tablePage;
		Activator.getDefault().setMetricsView(this);

		// createStatusBar(c);
		// getViewSite().getPage().addSelectionListener(this);
		// mActions = new MetricsActionGroup(this);
		cleanCache = new CacheCleanAction();		
		importXML = new ImportAction();
		exportXML = new ExportAction();
		
		IActionBars actionBars = getViewSite().getActionBars();
		actionBars.getToolBarManager().add(cleanCache);
		actionBars.getMenuManager().add(cleanCache);
		
		actionBars.getToolBarManager().add(new Separator());
		actionBars.getMenuManager().add(new Separator());
		
		actionBars.getToolBarManager().add(importXML);
		actionBars.getMenuManager().add(importXML);
		
		actionBars.getToolBarManager().add(exportXML);
		actionBars.getMenuManager().add(exportXML);		

		// MetricsPlugin.getDefault().addPropertyChangeListener(this);
		// MetricsBuilder.addMetricsProgressListener(this);
	}

	private class CacheCleanAction extends Action {
		
		public CacheCleanAction() {
			super(textCleanCache, Activator
					.getImageDescriptor("clean.png"));
			setToolTipText(textCleanCache);
		}

		public void run() {
			System.out.println("Clean the cache...");
			
			//remove all the results from the view
			table.removeAll();
			
			Job job = new Job("Cleaning the Cache ..") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					Cache.singleton.clear();		
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			job.schedule();			
		}
	}

	private class ExportAction extends Action {

		public ExportAction() {
			super(textExportXML, Activator
					.getImageDescriptor("export.gif"));
			setToolTipText(textExportXML);
		}

		public void run() {
			
			System.out.println("Export.....");

			if (rootSelection != null) {
				Shell activeShell = new Shell();
				FileDialog d = new FileDialog(activeShell, SWT.SAVE);
				String fileName = d.open();
				if (fileName != null) {
					File outputFile = new File(fileName);
					XMLExporter exporter;
					try {
						exporter = new XMLExporter(outputFile);
						doExport(activeShell, exporter);
					} catch (FileNotFoundException e) {
						logger.log(Level.WARNING,
								"MetricsView::File not found", e);
					} catch (InvocationTargetException e) {
						logger.log(Level.SEVERE, "MetricsView::doExport", e);
					} catch (InterruptedException e) {
						outputFile.delete();
						logger.log(Level.SEVERE, "MetricsView::doExport", e);
					}
				}
			} else {
				MessageDialog.openInformation(PlatformUI.getWorkbench()
						.getDisplay().getActiveShell(), "For Your Information",
						"You have to select a Java Element to metric first..");
			}
		}

		private void doExport(Shell activeShell, final XMLExporter exporter)
				throws InvocationTargetException, InterruptedException {

			IRunnableWithProgress op = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					exporter.export(rootSelection, monitor);
				}
			};
			new ProgressMonitorDialog(activeShell).run(true, true, op);
		}

	}

	private class ImportAction extends Action {

		public ImportAction() {
			super(textImportXML, Activator
					.getImageDescriptor("import.gif"));
			setToolTipText(textImportXML);
		}

		public void run() {
			System.out.println("Import.....");
			Shell activeShell = new Shell();
			FileDialog d = new FileDialog(activeShell, SWT.OPEN);
			String fileName = d.open();
			if (fileName != null) {
				System.out.println(fileName
						+ " selected for Import Function..");
				
				//WARNNING: FAKE!
				setSelection(rootSelection);
			}			
		}
	}

	@Override
	public void setFocus() {
		table.setFocus();
	}

	private void createTable(Composite c) {
		table = new MetricsResultTable(c, SWT.FULL_SELECTION);
		GridData data = new GridData(GridData.FILL_BOTH | SWT.H_SCROLL
				| SWT.V_SCROLL);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		table.setLayoutData(data);
		table.initWidths(memento);
		table.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				TreeItem item = (TreeItem) e.item;
				if (item != null) {
					supplementTitle(item);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void supplementTitle(TreeItem item) {
	}

	public void setSelection(AbstractMetricElement ame) {
		this.rootSelection = ame;
		if (ame != null) {
			refreshTable(ame);
			showTablePage();
		} else
			logger.warning("get null from selection.");
	}

	private void refreshTable(final AbstractMetricElement ms) {
		final Display display = Display.getDefault();
		display.asyncExec(new Runnable() {
	
			public void run() {
				if (!table.isDisposed()) {					
					table.setMetrics(ms);
					// table.setCursor(getNormalCursor(table.getDisplay()));
					setPartName(textMetricResult);
				}
			}
		});
	}

	private void showTablePage() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				pageSelector.topControl = tablePage;
				cards.layout();
				// mActions.enable();
			}
		});
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// TODO Auto-generated method stub
	}
}
