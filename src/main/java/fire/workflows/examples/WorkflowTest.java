package fire.workflows.examples;

import fire.context.JobContext;
import fire.context.JobContextImpl;
import fire.nodes.dataset.NodeDatasetStructured;
import fire.nodes.etl.NodeColumnFilter;
import fire.nodes.util.NodePrintFirstNRows;
import fire.nodes.save.NodeSaveParquet;
import fire.fs.hdfs.Delete;
import fire.spark.CreateSparkContext;
import fire.workflowengine.ConsoleWorkflowContext;
import fire.workflowengine.DatasetType;
import fire.workflowengine.Workflow;
import fire.workflowengine.WorkflowContext;
import org.apache.spark.api.java.JavaSparkContext;
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

        // structured node
        NodeDatasetStructured structured = new NodeDatasetStructured(1, "csv1 node", "data/cars.csv", DatasetType.CSV, ",",
                "id label f1 f2", "double double double double",
                "numeric numeric numeric numeric");
        wf.addNode(structured);

        // column filter node
        NodeColumnFilter filter = new NodeColumnFilter(2, "filter node", "f1 f2");
        wf.addLink(structured, filter);

        // delete the output directory
        Delete.deleteFile("parquet");

        // save as parquet file
        NodeSaveParquet save = new NodeSaveParquet(4, "save", "parquet");
        wf.addLink(filter, save);

        // print first 2 rows
        NodePrintFirstNRows printFirstNRows = new NodePrintFirstNRows(3, "print first rows", 2);
        wf.addLink(save, printFirstNRows);

        // execute the workflow
        wf.execute(jobContext);

    }
    
}

