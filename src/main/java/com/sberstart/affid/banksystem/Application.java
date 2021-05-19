package com.sberstart.affid.banksystem;

import com.sberstart.affid.banksystem.dao.DataSource;
import com.sberstart.affid.banksystem.service.BankService;

import java.sql.SQLException;
import java.util.Optional;

public class Application {

    public static void main(String[] args) throws SQLException {
        BankService service = new BankService(DataSource.getConnection());
        Optional<String> response = service.getClient(3);
        response.ifPresent(System.out::println);
    }
}
