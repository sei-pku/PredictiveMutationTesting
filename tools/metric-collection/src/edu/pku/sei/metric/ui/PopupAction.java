package edu.pku.sei.metric.ui;

import java.util.List;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import edu.pku.sei.metric.Activator;
import edu.pku.sei.metric.Constants;
import edu.pku.sei.metric.builder.FullMetricCaculator;
import edu.pku.sei.metric.source.AbstractMetricElement;
import edu.pku.sei.metric.source.FakeMetric;

/**
 * The delegate to deal with the pop up action "PKU full metric"
 *
 * @author liushi07
 *
 */
public class PopupAction implements IObjectActionDelegate, Constants {

	private ISelection selection;

	private Logger logger = Logger.getLogger(PopupAction.class.getName());

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub
	}

	public void showMetricView() throws PartInitException {
		IWorkbenchPage page = PlatformUI.getWorkbench().getWorkbenchWindows()[0]
				.getActivePage();
		page.showView("PKUMetric Plugin.MetricsView", null,
				IWorkbenchPage.VIEW_ACTIVATE);
	}

	public void run(IAction action) {

		if (!(this.selection instanceof IStructuredSelection))
			return;

		// show the metric view
		try {
			showMetricView();
		} catch (PartInitException e) {
			logger.throwing("PopupAction", "run", e);
		}

		IStructuredSelection structureSelection = (IStructuredSelection) this.selection;
		final List<?> selections = structureSelection.toList();
		if (selections.size() == 1) {
			Job job = new Job("Calculating Metrics ..") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					IJavaElement javaElement = (IJavaElement) selections.get(0);
					AbstractMetricElement metricElement = FullMetricCaculator
							.calculate(javaElement, monitor);
					Activator.getDefault().getMetricsView().setSelection(
							metricElement);
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		} else {
			Job job = new Job("Calculating Metrics ...") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					AbstractMetricElement parent = new FakeMetric();
					parent = FullMetricCaculator.calculate(selections, parent,
							monitor);
					Activator.getDefault().getMetricsView()
							.setSelection(parent);
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			job.schedule();

		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
