package com.yucfeng.springdemo.service;

import com.yucfeng.springdemo.bean.Entity;
import com.yucfeng.springdemo.bean.RequestBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class KVServiceImpl implements KVService {

    private static final long RETRY_INTERVAL = 500L;
    private static final int retryCount = 10;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public String doGetValue(String key, boolean force) throws InterruptedException {
        Optional<Entity> entityOptional = query(key, force);
        return entityOptional.map(Entity::getValue).orElse(null);
    }

    private Optional<Entity> query(String key, boolean force) throws InterruptedException {
        Assert.isTrue(!key.contains("'"), "invalid key!");
        List<Entity> list = getEntities(key);
        log.info("query result {}", list.size());
        if (list.isEmpty()) {
            return Optional.empty();
        }
        Entity res = list.get(0);
        if (force) {
            return Optional.of(res);
        }

        boolean inChange = res.getInChange() == 1;
        if (inChange) {
            log.info("This record is locked by other transaction, wait and retry");
            int count = 0;
            while (count < retryCount) {
                Thread.sleep(RETRY_INTERVAL);
                list = getEntities(key);
                count++;
                if (list.isEmpty()) {
                    return Optional.empty();
                }
                res = list.get(0);
                if (res.getInChange() == 0) {
                    return Optional.of(res);
                }
            }
            log.warn("Suspect that there is a long transaction happening or the transaction owner is dead. Return the value.");
        }
        return Optional.of(res);
    }

    @Override
    public Optional<Entity> doInsertOrUpdateValue(RequestBean requestBean) throws SQLException {
        String key = requestBean.getKey();
        Assert.notNull(key, "key cannot be null!");
        Assert.isTrue(!key.contains("'"), "invalid key!");

        try {
            Optional<Entity> entityOptional = query(key, false);
            if (entityOptional.isEmpty()) {
                log.info("key {} does not exist, do insert.", key);
                addKV(requestBean);
            } else {
                log.info("key {} exists, do update.", key);
                boolean inChange = entityOptional.get().getInChange() == 1;
                if (inChange) {
                    log.info("This record is locked by other transaction, wait and retry");
                    int count = 0;
                    while (count < retryCount) {
                        Thread.sleep(RETRY_INTERVAL);
                        List<Entity> list = getEntities(key);
                        count++;
                        if (list.isEmpty()) {
                            break;
                        }
                        Entity res = list.get(0);
                        if (res.getInChange() == 0) {
                            break;
                        }
                    }
                    log.warn("Suspect that there is a long transaction happening or the transaction owner is dead. Throw exception.");
                    throw new SQLException("Double writing is forbidden!");
                }
                lock(key);

                updateKV(requestBean);
            }
            return query(key, true);
        } catch (Exception e) {
            log.error("doInsertOrUpdateValue for {} failed", requestBean, e);
            throw new SQLException(e);
        } finally {
            unlock(key);
        }
    }

    /*
     The following code should be in DAO layer if DAO layer exists.
     */
    private List<Entity> getEntities(String key) {
        String sql = "select key, value, inChange from kv where key =?";

        PreparedStatementCreatorFactory psCreatorFactory =
                new PreparedStatementCreatorFactory(sql, Types.VARCHAR);
        return jdbcTemplate.query(psCreatorFactory.newPreparedStatementCreator(new Object[]{key}),
                new BeanPropertyRowMapper<>(Entity.class));
    }

    public void addKV(RequestBean requestBean) {
        final String sql = "insert into kv(key, value) values (?,?)";
        jdbcTemplate.update(sql, requestBean.getKey(), requestBean.getValue());
    }

    public void updateKV(RequestBean requestBean) {
        final String sql = "update kv set value=? where key=?";
        jdbcTemplate.update(sql, requestBean.getValue(), requestBean.getKey());
    }

    private void lock(String key) {
        String sql = "update kv set inChange='1' where key=?";
        jdbcTemplate.update(sql, key);
    }

    private void unlock(String key) {
        String sql = "update kv set inChange='0' where key=?";
        jdbcTemplate.update(sql, key);
    }
}
