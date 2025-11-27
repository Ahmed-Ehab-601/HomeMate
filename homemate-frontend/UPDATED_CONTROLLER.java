@RestController
@RequestMapping("/service")
@CrossOrigin(origins = "http://localhost:5173") // ADD THIS LINE - Fixes CORS issue
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceManagService serviceManagService;

    @GetMapping("/getallservices")
    public List<ServiceDto> getAllServices() throws SQLException {
        return serviceManagService.getAllService();
    }

    @GetMapping("/getservicedetails/{id}")
    public ServiceDetailsDto getDetails(@PathVariable long id) throws SQLException {
        return serviceManagService.getDetails(id);
    }

    @PostMapping("/create")
    public String createService(@RequestBody ServiceDto dto) throws SQLException {
        serviceManagService.createService(dto);
        return "Service created successfully";
    }

    @PostMapping("/edit/{id}")
    public String editService(@PathVariable long id, @RequestBody ServiceDto dto) throws SQLException {
        serviceManagService.editService(id, dto);
        return "Service updated successfully";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteService(@PathVariable long id) throws SQLException {
        serviceManagService.deleteService(id);
        return "Service deleted successfully";
    }
}

