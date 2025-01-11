package com.gsc.gsc.store.service.serviceImplementation;

import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.model.SellerBrand;
import com.gsc.gsc.model.Store;
import com.gsc.gsc.model.StoreText;
import com.gsc.gsc.model.User;
import com.gsc.gsc.repo.SellerBrandRepository;
import com.gsc.gsc.repo.StoreDetailsRepository;
import com.gsc.gsc.repo.StoreRepository;
import com.gsc.gsc.repo.UserRepository;
import com.gsc.gsc.store.dto.StoreDTO;
import com.gsc.gsc.store.service.serviceInterface.IStoreService;
import com.gsc.gsc.user.security.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.gsc.gsc.constants.UserTypes.ADMIN_TYPE;
import static com.gsc.gsc.utilities.LanguageConstants.ARABIC;
import static com.gsc.gsc.utilities.LanguageConstants.ENGLISH;

@Service
public class StoreService implements IStoreService {

    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private SellerBrandRepository sellerBrandRepository;
    @Autowired
    private StoreDetailsRepository storeDetailsRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    public Integer getUserIdFromToken(String token) {

        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        String userIdString = claims.getSubject();
        System.out.println("getProperty " + token + " ,  " + userIdString);
        return Integer.parseInt(userIdString);
    }

    @Override
    public Optional<Store> getById(Integer id) {
        return storeRepository.findById(id);
    }

    public List<SellerBrand> getFirst4Stores() {
        return sellerBrandRepository.findAll();

    }

    @Override
    public ResponseEntity create(String token, StoreDTO dto) {
        ReturnObject returnObject = new ReturnObject();
        Integer userId = getUserIdFromToken(token);
        User userAdmin = userRepository.findUserById(userId);
        if (userId != null && userAdmin.getAccountTypeId() == ADMIN_TYPE) {
            Optional<Store> optionalStore = storeRepository.findByCode(dto.getCode());
            if (optionalStore.isPresent()) {
                returnObject.setData(optionalStore.get());
                returnObject.setMessage("This store code is already exists");
                returnObject.setStatus(false);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
            }

            Store store = new Store();
            store.setCode(dto.getCode());
            store.setLocation(dto.getLocation());
            store = storeRepository.save(store);

            StoreText storeTextEn = new StoreText();
            storeTextEn.setStoreId(store.getId());
            storeTextEn.setName(dto.getNameEn());
            storeTextEn.setDescription(dto.getDescriptionEn());
            storeTextEn.setLangId(ENGLISH);

            StoreText storeTextAr = new StoreText();
            storeTextAr.setStoreId(store.getId());
            storeTextAr.setName(dto.getNameAr());
            storeTextAr.setDescription(dto.getDescriptionAr());
            storeTextAr.setLangId(ARABIC);
            storeDetailsRepository.save(storeTextEn);
            storeDetailsRepository.save(storeTextAr);

            dto.setId(store.getId());

            returnObject.setData(dto);
            returnObject.setStatus(true);
            returnObject.setMessage("Created Successfully");
            return ResponseEntity.ok(returnObject);
        } else {
            returnObject.setStatus(false);
            returnObject.setData(null);
            returnObject.setMessage("This user isn't Authorized");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }

    @Override
    public ResponseEntity<?> getStores(String token) {
        ReturnObject returnObject = new ReturnObject();
        Integer userId = getUserIdFromToken(token);
        User userAdmin = userRepository.findUserById(userId);
        if (userId != null && userAdmin.getAccountTypeId() == ADMIN_TYPE) {
            List<Store> storesList = storeRepository.findAll();
            List<StoreDTO> storeDTOSList = new ArrayList();
            for (int i = 0; i < storesList.size(); i++) {
                StoreDTO storeDTO = new StoreDTO();
                storeDTO.setId(storesList.get(i).getId());
                storeDTO.setCode(storesList.get(i).getCode());
                storeDTO.setLocation(storesList.get(i).getLocation());

                Optional<StoreText> storeDetailsEn = storeDetailsRepository.findByStoreIdAndLangId(storesList.get(i).getId(), ENGLISH);
                Optional<StoreText> storeDetailsAr = storeDetailsRepository.findByStoreIdAndLangId(storesList.get(i).getId(), ARABIC);

                storeDTO.setNameEn(storeDetailsEn.get().getName());
                storeDTO.setDescriptionEn(storeDetailsEn.get().getDescription());
                storeDTO.setNameAr(storeDetailsAr.get().getName());
                storeDTO.setDescriptionAr(storeDetailsAr.get().getDescription());
                storeDTOSList.add(storeDTO);

            }
            returnObject.setMessage("Loaded Successfully");
            returnObject.setStatus(true);
            returnObject.setData(storeDTOSList);
        }else{
            returnObject.setData(null);
            returnObject.setStatus(false);
            returnObject.setMessage("This user isn't Authorized");
        }
        return ResponseEntity.ok(returnObject);

    }

    @Override
    public ResponseEntity update(String token, Integer storeId, StoreDTO dto) {
        ReturnObject returnObject = new ReturnObject();
        Integer userId = getUserIdFromToken(token);
        User userAdmin = userRepository.findUserById(userId);

        if (userId != null && userAdmin.getAccountTypeId() == ADMIN_TYPE) {
            Optional<Store> existingStoreOptional = storeRepository.findById(storeId);

            if (!existingStoreOptional.isPresent()) {
                returnObject.setData(null);
                returnObject.setMessage("Store not found");
                returnObject.setStatus(false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(returnObject);
            }

            Store existingStore = existingStoreOptional.get();

            // Check if the store code is being updated to an already existing code
            if (!existingStore.getCode().equals(dto.getCode())) {
                Optional<Store> optionalStore = storeRepository.findByCode(dto.getCode());
                if (optionalStore.isPresent()) {
                    returnObject.setData(optionalStore.get());
                    returnObject.setMessage("This store code is already exists");
                    returnObject.setStatus(false);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
                }
            }

            existingStore.setCode(dto.getCode());
            existingStore.setLocation(dto.getLocation());
            storeRepository.save(existingStore);

            // Update or create StoreText records for different languages
            updateOrCreateStoreText(storeId, dto.getNameEn(), dto.getDescriptionEn(), ENGLISH);
            updateOrCreateStoreText(storeId, dto.getNameAr(), dto.getDescriptionAr(), ARABIC);

            dto.setId(existingStore.getId());

            returnObject.setData(dto);
            returnObject.setStatus(true);
            returnObject.setMessage("Updated Successfully");
            return ResponseEntity.ok(returnObject);
        } else {
            returnObject.setStatus(false);
            returnObject.setData(null);
            returnObject.setMessage("This user isn't Authorized");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }

    private void updateOrCreateStoreText(Integer storeId, String name, String description, Integer langId) {
        Optional<StoreText> existingStoreTextOptional = storeDetailsRepository.findByStoreIdAndLangId(storeId, langId);

        if (existingStoreTextOptional.isPresent()) {
            StoreText existingStoreText = existingStoreTextOptional.get();
            existingStoreText.setName(name);
            existingStoreText.setDescription(description);
            storeDetailsRepository.save(existingStoreText);
        } else {
            StoreText newStoreText = new StoreText();
            newStoreText.setStoreId(storeId);
            newStoreText.setName(name);
            newStoreText.setDescription(description);
            newStoreText.setLangId(langId);
            storeDetailsRepository.save(newStoreText);
        }
    }

    @Override
    public ResponseEntity delete(Integer id) {
        return null;
    }

}
