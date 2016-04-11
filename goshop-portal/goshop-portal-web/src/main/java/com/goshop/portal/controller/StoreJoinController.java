package com.goshop.portal.controller;

import com.goshop.common.attachment.AttachmentService;
import com.goshop.common.exception.PageException;
import com.goshop.manager.pojo.Store;
import com.goshop.manager.pojo.StoreJoin;
import com.goshop.manager.pojo.User;
import com.goshop.portal.i.StoreInfoModel;
import com.goshop.portal.i.StoreJoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping(value = "/store_join")
public class StoreJoinController {

    @Autowired
    StoreJoinService storeJoinService;

    @Autowired
    AttachmentService attachmentService;

    @RequestMapping("/agreement")
    public String agreement(Model model,
                            @RequestParam(value = "state", required = false) String statePage,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        String jump = jump(statePage);
        if (jump != null) {
            return jump;
        }
        model.addAttribute("P_STEP", 1);
        return "store/store_agreement";

    }

    public String jump(String statePage) {
        /*Store store = storeService.getCurrentStore();
        if (store != null) {
            return "redirect:/se/goods/addstep/one";
        }
        StoreJoinin storeJoinin = storeJoininService.getCurrentUserStoreJoinin();
        if (storeJoinin != null) {
            String state = storeJoinin.getJoininState();
            if (StringUtils.hasText(state) && state.equals("30") && !StringUtils.hasText(statePage)) {
                return "redirect:/storejoinin/step1";
            } else if (StringUtils.hasText(state)) {
                return "redirect:/storejoinin/step4";
            }
        }*/

        return null;
    }

    @RequestMapping("/step1")
    public String stepOne(Model model,
                          HttpServletRequest request,
                          HttpServletResponse response) {
        model.addAttribute("P_STEP", 1);
        model.addAttribute("P_SIDEBAR", 1);
        return "store/settled_step_one";

    }

    @RequestMapping(value = "/step2", method = RequestMethod.POST)
    public String stepTwo(User user,StoreJoin storeJoin,
                          BindingResult result,
                          Model model,
                          @RequestParam("businessLicenceNumber_electronic") MultipartFile businessLicenceNumberFile,//营业执照号电子版
                          @RequestParam("organization_code_electronic") MultipartFile organizationCodeFile,//组织机构代码证电子版
                          @RequestParam("general_taxpayer") MultipartFile generalTaxpayerFile,//一般纳税人证明
                          HttpServletRequest request,
                          HttpServletResponse response) {
        Assert.isTrue(businessLicenceNumberFile.getSize() > 0, "请上传营业执照号电子版！");
        Assert.isTrue(businessLicenceNumberFile.getSize() < 1000000, "营业执照号电子版文件超过了1M，请编辑后重新上传！");

        Assert.isTrue(organizationCodeFile.getSize() > 0, "请上传组织机构代码证电子版！");
        Assert.isTrue(organizationCodeFile.getSize() < 1000000, "组织机构代码证电子版文件超过了1M，请编辑后重新上传！");

        Assert.isTrue(generalTaxpayerFile.getSize() > 0, "请上传一般纳税人证明！");
        Assert.isTrue(generalTaxpayerFile.getSize() < 1000000, "一般纳税人证明文件超过了1M，请编辑后重新上传！");

        try {
            String business= attachmentService.upload(businessLicenceNumberFile);
            String organization =  attachmentService.upload(organizationCodeFile);
            String generalTaxpayer=attachmentService.upload(generalTaxpayerFile);
            storeJoin.setBusinessLicenceNumberElectronic(business);
            storeJoin.setOrganizationCodeElectronic(organization);
            storeJoin.setGeneralTaxpayer(generalTaxpayer);
        } catch (IOException e) {
            e.printStackTrace();
            throw new PageException("文件保存错误！");
        }

        try {
            storeJoinService.applySeller(user,storeJoin);
        }catch (Exception e){
            throw new PageException(e.getMessage());
        }
        model.addAttribute("P_STEP", 2);
        model.addAttribute("P_SIDEBAR", 2);
        return "store/settled_step_two";
    }

    @RequestMapping(value = "/step3", method = RequestMethod.POST)
    public String stepThree(User user,StoreJoin storeJoin,
                            Model model,
                            @RequestParam("bank_licence_electronic") MultipartFile bankLicenceElectronicFile,//开户银行许可证电子版
                            @RequestParam("taxRegistrationCertificate_electronic") MultipartFile taxRegistrationCertificateElectronicFile,//税务登记证号电子版
                            HttpServletRequest request,
                            HttpServletResponse response) {


        Assert.isTrue(bankLicenceElectronicFile.getSize() > 0, "请上传开户银行许可证电子版！");
        Assert.isTrue(bankLicenceElectronicFile.getSize() < 1000000, "开户银行许可证电子版文件超过了1M，请编辑后重新上传！");

        Assert.isTrue(taxRegistrationCertificateElectronicFile.getSize() > 0, "请上传税务登记证号电子版！");
        Assert.isTrue(taxRegistrationCertificateElectronicFile.getSize() < 1000000, "税务登记证号电子版文件超过了1M，请编辑后重新上传！");

        try {
            String bankLicence= attachmentService.upload(bankLicenceElectronicFile);
            String taxRegistrationCertificate =  attachmentService.upload(taxRegistrationCertificateElectronicFile);
            storeJoin.setBankLicenceElectronic(bankLicence);
            storeJoin.setTaxRegistrationCertificateElectronic(taxRegistrationCertificate);
        } catch (IOException e) {
            e.printStackTrace();
            throw new PageException("文件保存错误！");
        }

        try {
            StoreInfoModel storeInfoModel=storeJoinService.applySellerThree(user,storeJoin);
            model.addAttribute("P_PARENT_STORECLASS", storeInfoModel.getStoreClassParentList());
            model.addAttribute("P_PARENT_GOODSCLASS", storeInfoModel.getGoodsClassParentList());
            model.addAttribute("P_STOREGRADE", storeInfoModel.getStoreGradeList());
        }catch (Exception e){
            throw new PageException("保存财务信息错误："+e.toString());
        }

        model.addAttribute("P_STEP", 3);
        model.addAttribute("P_SIDEBAR", 3);
        return "settled_step_three";

    }

}