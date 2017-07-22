package fire.workflows.examples;

import fire.context.JobContext;
import fire.context.JobContextImpl;
import fire.nodes.dataset.NodeDatasetStructured;
import fire.nodes.etl.NodeColumnFilter;
import fire.nodes.examples.NodeTestConcatColumns;
import fire.nodes.examples.NodeTestPrintFirstNRows;
import fire.spark.CreateSparkContext;
import fire.workflowengine.ConsoleWorkflowContext;
import fire.workflowengine.DatasetType;
import fire.workflowengine.Workflow;
import fire.workflowengine.WorkflowContext;
import org.apache.spark.sql.SparkSession;

public class WorkflowTest {

    //--------------------------------------------------------------------------------------

    public static void main(String[] args) {

        // create spark session
        SparkSession ctx = CreateSparkContext.createSession(args);;
        // create workflow context
        WorkflowContext workflowContext = new ConsoleWorkflowContext();
        // create job context
        JobContext jobContext = new JobContextImpl(ctx, workflowContext);

        try {
            filterwf(jobContext);
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        // stop the context
        ctx.stop();
    }

    //--------------------------------------------------------------------------------------

    // filter columns workflow workflow
    private static void filterwf(JobContext jobContext) throws Exception {

        Workflow wf = new Workflow();

        // NODE IDS HAVE TO BE UNIQUE
        
        // structured node
        NodeDatasetStructured structured = new NodeDatasetStructured(1, "csv1 node", "data/cars.csv", DatasetType.CSV, ",",
                "id label f1 f2", "double double double double",
                "numeric numeric numeric numeric");
        wf.addNode(structured);

        // column filter node
        NodeColumnFilter filter = new NodeColumnFilter(5, "filter node", "f1 f2");
        wf.addLink(structured, filter);

        // concat columns node
        NodeTestConcatColumns concatColumns = new NodeTestConcatColumns(10, "filter node", "f1 f2", "f1f2", "|");
        wf.addLink(filter, concatColumns);

        // print first 10 rows
        NodeTestPrintFirstNRows printFirstNRows = new NodeTestPrintFirstNRows(15, "print first rows", 10);
        wf.addLink(concatColumns, printFirstNRows);

        // execute the workflow
        wf.execute(jobContext);

    }
    
}

