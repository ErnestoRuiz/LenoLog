package co.ReaperDev.service;

import co.ReaperDev.dto.CarDTO;
import co.ReaperDev.dto.ServiceDTO;
import co.ReaperDev.dto.ServicesMetrics;
import co.ReaperDev.dto.UserDTO;
import co.ReaperDev.repository.ServiceRepository;
import co.ReaperDev.repository.entity.CarEntity;
import co.ReaperDev.repository.entity.CostEntity;
import co.ReaperDev.repository.entity.ServiceEntity;
import co.ReaperDev.repository.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class ServiceService {
    private ServiceRepository servRepo;
    private MapperFacade mapper;

    public void createService(ServiceDTO serviceDTO){
        log.info("ServiceService.createService()");
        ServiceEntity entity = mapper.map(serviceDTO, ServiceEntity.class);
        entity.setDate(Date.valueOf(serviceDTO.getDate()));
        servRepo.createService(entity);
    }

    public List<ServiceDTO> getServicesByCar(int carId){
        log.info("ServiceService.getServiceByCarId()");
        List<ServiceEntity> entities = servRepo.getServicesByCar(carId);
        List<ServiceDTO> retval = new ArrayList<>();
        for(ServiceEntity s: entities){
            ServiceDTO serviceDTO = mapper.map(s, ServiceDTO.class);
            serviceDTO.setDate(s.getDate().toString());
            retval.add(serviceDTO);
        }
        return retval;
    }

    public List<ServiceDTO> getServicesByUser(UserDTO userDTO){
        log.info("ServiceService.getServiceByUser()");
        UserEntity userEntity = mapper.map(userDTO, UserEntity.class);
        List<ServiceEntity> entities = servRepo.getServicesByUser(userEntity);
        List<ServiceDTO> retval = new ArrayList<>();
        for(ServiceEntity s: entities){
            ServiceDTO serviceDTO = mapper.map(s, ServiceDTO.class);
            serviceDTO.setDate(s.getDate().toString());
            retval.add(serviceDTO);
        }
        return retval;
    }

    public void deleteService(int serviceId){
        log.info("deleteService endpoint hit");
        servRepo.deleteService(serviceId);
    }

    public double totalCostForYear(List<CostEntity> list){
        double retval = 0;
        for(CostEntity c: list){
            retval = retval + c.getCost();
        }
        BigDecimal bigDecimal = new BigDecimal(retval).setScale(2, RoundingMode.HALF_UP);
        double rounded = bigDecimal.doubleValue();
        return rounded;
    }

    public double getMonthlyAverageForYear(double d){
        double retval = d/12;
        BigDecimal bigDecimal = new BigDecimal(retval).setScale(2, RoundingMode.HALF_UP);
        double rounded = bigDecimal.doubleValue();
        return rounded;
    }

    public double monthlyAverageCurrentYear(List<CostEntity> list){
        double total = totalCostForYear(list);
        LocalDate localDate = LocalDate.now();
        double month = localDate.getMonthValue();
        double retval = total / month;
        BigDecimal bigDecimal = new BigDecimal(retval).setScale(2, RoundingMode.HALF_UP);
        double rounded = bigDecimal.doubleValue();
        return rounded;
    }

    public ServicesMetrics getCostOfServices(int carId){
        log.info("CarService.getCostOfServices()");

        List<CostEntity> totalCostList = servRepo.getListOfTotalCost(carId);
        List<CostEntity> pastYearCostList = servRepo.getListOfPastYearCost(carId);

        ServicesMetrics servicesMetrics = new ServicesMetrics();
        servicesMetrics.setCarId(carId);
        servicesMetrics.setTotalCostAllServices(totalCostForYear(totalCostList));
        servicesMetrics.setTotalCostPastTwelveMo(totalCostForYear(pastYearCostList));
        servicesMetrics.setPastTwelveMonthAverage(getMonthlyAverageForYear(totalCostForYear(pastYearCostList)));

        List<CostEntity> currentYear = servRepo.currentYearCostList(carId);
        List<CostEntity> yearOne = servRepo.getListOfCostForYear(carId, 1);
        log.info(yearOne.toString());
        List<CostEntity> yearTwo = servRepo.getListOfCostForYear(carId, 2);
        List<CostEntity> yearThree = servRepo.getListOfCostForYear(carId, 3);
        List<CostEntity> yearFour = servRepo.getListOfCostForYear(carId, 4);
        List<CostEntity> yearFive = servRepo.getListOfCostForYear(carId, 5);
        List<CostEntity> fiveYearTotal = servRepo.fiveYearTotalCost(carId);

        servicesMetrics.setTotalCostCurrentYear(totalCostForYear(currentYear));
        servicesMetrics.setTotalCostLastYear(totalCostForYear(yearOne));
        servicesMetrics.setTotalCostTwoYearsAgo(totalCostForYear(yearTwo));
        servicesMetrics.setTotalCostThreeYearsAgo(totalCostForYear(yearThree));
        servicesMetrics.setTotalCostFourYearsAgo(totalCostForYear(yearFour));
        servicesMetrics.setTotalCostFiveYearsAgo(totalCostForYear(yearFive));
        servicesMetrics.setFiveYearTotalCost(totalCostForYear(fiveYearTotal)); //recycling method for it's math

        servicesMetrics.setMonthlyAverageCurrentYear(monthlyAverageCurrentYear(currentYear));
        servicesMetrics.setMonthlyAverageLastYear(getMonthlyAverageForYear(totalCostForYear(yearOne)));
        servicesMetrics.setMonthlyAverageTwoYearsAgo(getMonthlyAverageForYear(totalCostForYear(yearTwo)));
        servicesMetrics.setMonthlyAverageThreeYearsAgo(getMonthlyAverageForYear(totalCostForYear(yearThree)));
        servicesMetrics.setMonthlyAverageFourYearsAgo(getMonthlyAverageForYear(totalCostForYear(yearFour)));
        servicesMetrics.setMonthlyAverageFourYearsAgo(getMonthlyAverageForYear(totalCostForYear(yearFive)));

        return servicesMetrics;

    }
}
