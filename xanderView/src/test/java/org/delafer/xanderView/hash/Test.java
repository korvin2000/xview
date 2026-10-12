package org.delafer.xanderView.hash;

import java.util.Set;
import java.util.TreeSet;

import net.j7.commons.utils.ByteUtils;
import net.j7.commons.utils.Metrics;
import net.jpountz.xxhash.XXHashFactory;

import org.delafer.xanderView.file.CopyService;

public class Test {
	int ab;
	int cd;
	public static void main(String[] args) {


		CopyService cs = CopyService.instance();
		cs.init();
//		cs.test();
		System.exit(0);

		XXHashFactory hash = XXHashFactory.fastestInstance();
		System.out.println(hash);

		Set<Long> m = new TreeSet<Long>();

		long ii= 0;
		Metrics m1 = Metrics.start();
		for (long i = 10000000; i < 20100000; i++) {
			ii = i;
			byte[] buf = ByteUtils.longToByteArray(ii);
			long res = hash.hash64().hash(buf, 0, buf.length, 65536);
			if (m.contains(res)) System.out.println("Collision: "+i);
			m.add(res);
		}
		m1.measure("done ");
		System.out.println(m.size());


	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ab;
		result = prime * result + cd;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Test other = (Test) obj;
		if (ab != other.ab)
			return false;
		if (cd != other.cd)
			return false;
		return true;
	}

}
