package rpc;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.MySQLConnection;
import entity.Item;
import external.GithubJobClient;

/**
 * Servlet implementation class save
 */
@WebServlet("/save")
public class Save extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Save() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userId = request.getParameter("user_id");
		JSONArray array = new JSONArray();
		
		MySQLConnection connection = new MySQLConnection();
		Set<String> savedJobIds = connection.getSavedJobs(userId);
		connection.close();
		
		GithubJobClient client = new GithubJobClient();
		
		for (String id : savedJobIds) {
			Item job = client.getJobfromJobId(id);
			JSONObject obj = job.toJSONObject();
			try {
				obj.append("savedJob", true);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			array.put(obj);
		}
		RpcHelper.writeJsonArray(response, array);
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		JSONObject input = RpcHelper.readJSONObject(request);
		try {
			//payload expected {"user_id" : "xxx", "job_id" : "xxxxx" , "is_save" : "true" }
			String userId = input.getString("user_id");
			String jobId = input.getString("job_id");
			String isSave = input.getString("is_save");

			MySQLConnection connection = new MySQLConnection();
			if (isSave == "true") {
				connection.setSavedJob(userId, jobId);
			} else {
				connection.unSetSavedJob(userId, jobId);
			}
			
			connection.close();
			RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
