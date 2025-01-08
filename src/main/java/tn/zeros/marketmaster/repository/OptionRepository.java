package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.Option;

public interface OptionRepository extends JpaRepository<Option,Long> {

}
