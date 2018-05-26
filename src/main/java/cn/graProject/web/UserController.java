package cn.graProject.web;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;

import cn.graProject.entity.Behave;
import cn.graProject.entity.Device;
import cn.graProject.entity.DiseaseCase;
import cn.graProject.entity.Fish;
import cn.graProject.entity.TreatmentCase;
import cn.graProject.entity.User;
import cn.graProject.service.CaseService;
import cn.graProject.service.DataService;
import cn.graProject.service.PageService;

@Controller
public class UserController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DataService dataService;
	@Autowired
	private CaseService caseService;
	@Autowired
	private PageService pageService;

	/**
	 * 从login action 登陆成功后重定向过来
	 * 
	 * @param userId
	 * @param model
	 * @param httpSession
	 * @return 用户个人主页页面 含有设备的信息
	 */
	@RequestMapping(value = "/personal/{userId}")
	public String userLoginSuccess(@PathVariable("userId") String userId, Model model, HttpSession httpSession) {
		User user = (User) httpSession.getAttribute("user");
		Device device = dataService.findNewData(user.getUserDev());
		model.addAttribute("device", device);
		// model.addAttribute("user", user);
		// change database table user_info :
		// ->change user_id to user_name (varchar)
		// ->add colume user_id PRI (int)
		return "personal";
	}

	
	@RequestMapping(value="/historyList")
	public String histroyList(HttpSession httpSession,@RequestParam int page, @RequestParam int pageSize, Model model) {
		//页码处理
		int totalPage = pageService.getDataTotalPage(pageSize);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("totalPage", totalPage);
		User user = (User) httpSession.getAttribute("user");
		List<Device> historyList=dataService.findDataByPage(user.getUserDev(),page,pageSize);
		
		model.addAttribute("historyList", historyList);
		return "historyList";
		
	}
	
	/**
	 * 历史数据折线图 异步请求
	 * 
	 * @param httpSession
	 * @return
	 */
	@RequestMapping(value = "/history", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> historyData(HttpSession httpSession) {
		User user = (User) httpSession.getAttribute("user");
		return dataService.findAllData(user.getUserDev());
	}

	
	
	/**
	 * 案例分析—>填写案例特征
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/caseAnalysis")
	public String caseAnalysis(Model model) {
		List<Fish> fishList = caseService.findAllFishType();
		List<Behave> behaveList = caseService.findAllBehave();
		model.addAttribute("behaveList", behaveList);
		model.addAttribute("fishList", fishList);
		return "caseAnalysis";
	}

	/**
	 * 查看案例 向页面返回案例列表
	 * @param page
	 * @param pageSize
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/caseList")
	public String caseList(@RequestParam int page, @RequestParam int pageSize, Model model) {
		int totalPage = pageService.getCaseTotalPage(pageSize);
		List<TreatmentCase> treatmentCaseList = caseService.findTreatmentCase(page, pageSize);

		model.addAttribute("treatmentCaseList", treatmentCaseList);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("totalPage", totalPage);
		return "caseList";

	}

	/**
	 * 在案例列表里点某个案例后 案例详情
	 * @param caseId
	 * @param diseaseId
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/caseInfo/{caseId}/diseaseId/{diseaseId}")
	public String caseInfo(@PathVariable("caseId") int caseId,@PathVariable("diseaseId") int diseaseId,Model model) {
		TreatmentCase treatmentCase=caseService.findTreatmentCaseInfoById(caseId);
		DiseaseCase diseaseCase=caseService.findDiseaseCaseInfoById(diseaseId);
		
		model.addAttribute("treatmentCase", treatmentCase);
		model.addAttribute("diseaseCase",diseaseCase);
		return "caseInfo";
	}
	
	/**
	 * 转向案例添加页面
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/caseAdd")
	public String caseAdd(Model model) {
		List<Fish> fishList = caseService.findAllFishType();
		List<DiseaseCase> diseaseList=caseService.findAllDiseaseCase();
		String fishJson=JSONArray.toJSONString(fishList);
		String diseaseJson=JSONArray.toJSONString(diseaseList);
		model.addAttribute("fishList", fishJson);
		model.addAttribute("diseaseList",diseaseJson);
		return "caseAdd";
		
	}
	
	/**
	 * 案例添加 提交审核
	 * @param treatmentCheck
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/caseSubmit")
	public String caseSubmit(TreatmentCase treatmentCheck,Model model) {
		caseService.addTreatmentCheck(treatmentCheck);
		return "redirect:/caseAdd";
	}
	

}
