/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tool;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author Administrator
 */
public class LogFormatter extends Formatter{

        @Override
        public String format(LogRecord record) {
                return record.getLevel() + ":" + record.getMessage()+"\n";
        }
}
