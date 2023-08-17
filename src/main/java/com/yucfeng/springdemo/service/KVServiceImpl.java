package com.yucfeng.springdemo.service;

import com.yucfeng.springdemo.bean.Entity;
import com.yucfeng.springdemo.bean.RequestBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class KVServiceImpl implements KVService {    // TODO prevent SQL inject.

    private static final long RETRY_INTERVAL = 500L;
    private static final int retryCount = 10;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public String doGetValue(String key, boolean force) throws InterruptedException {
        Map<String, Object> res = query(key, force);
        return res.isEmpty() ? null : (String) res.get("value");
    }

    private Map<String, Object> query(String key, boolean force) throws InterruptedException {
        Assert.isTrue(!key.contains("'"), "invalid key!");
        // TODO: use prepared statement to prevent SQL injection.
        String sql1 = "select key, value, inChange from kv where key =?";

        PreparedStatementCreatorFactory psCreatorFactory =
                new PreparedStatementCreatorFactory(sql1, Types.VARCHAR);
        List<Entity> entityList = jdbcTemplate.query(psCreatorFactory.newPreparedStatementCreator(new Object[] {key}),
                new BeanPropertyRowMapper<>(Entity.class));

        String sql = "select * from kv where key ='" + key + "'";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        log.info("query result {}", list.size());
        if (list.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, Object> res = list.get(0);
        if (force) {
            return res;
        }

        boolean inChange = (Integer) res.get("inChange") == 1;
        if (inChange) {
            log.info("This record is locked by other transaction, wait and retry");
            int count = 0;
            while (count < retryCount) {
                Thread.sleep(RETRY_INTERVAL);
                list = jdbcTemplate.queryForList(sql);
                count++;
                if (list.isEmpty()) {
                    return new HashMap<>();
                }
                res = list.get(0);
                if ((Integer) res.get("inChange") == 0) {
                    return res;
                }
            }
            log.warn("Suspect that there is a long transaction happening or the transaction owner is dead. Return the value.");
            return res;
        } else {
            return res;
        }
//        return new HashMap<>();
    }

    @Override
    public Map<String, Object> doInsertOrUpdateValue(RequestBean entity) throws SQLException {
        String key = entity.getKey();
        Assert.notNull(key, "key cannot be null!");
        Assert.isTrue(!key.contains("'"), "invalid key!");

        try {
            Map<String, Object> res = query(key, false);
            String sql;
            if (res.isEmpty()) {
                log.info("key {} does not exist, do insert.", key);
                String value = entity.getValue();
                sql = "insert into kv(key, value) values ('" + key + "', '" + value + "')";
                jdbcTemplate.update(sql);
            } else {
                log.info("key {} exists, do update.", key);
                lock(key);

                sql = "update kv set value='" + entity.getValue() + "' where key='" + key + "'";
                jdbcTemplate.update(sql);
            }
            return query(key, true);
        } catch (Exception e) {
            log.error("doInsertOrUpdateValue for {} failed", entity, e);
            throw new SQLException(e);
        } finally {
            unlock(key);
        }
    }

    private void lock(String key) {
        String sql = "update kv set inChange='1' where key='" + key + "'";
        jdbcTemplate.update(sql);
    }

    private void unlock(String key) {
        String sql = "update kv set inChange='0' where key='" + key + "'";
        jdbcTemplate.update(sql);
    }
}
